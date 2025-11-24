# StaticPermissions

> Statically checked, declarative access controls for Spring applications.

## Overview

Permission checks are typically a runtime construct. An application may check
permissions in its controllers, in its services, or both, with each approach
having its own pros and cons. This library provides an alternative approach,
where required permissions are represented as method parameters. Unifying
permission checks with the type system allows compile-time validation that 
appropriate access checks are performed, so long as permission-bearing types are
only instantiated through mechanisms that perform them - of which this
library provides several. In effect, application code declares what permissions
are needed, and this library provides appropriate instances, or refuses to do so
if access is denied.

## Setup

To enable this library, place `@EnableStaticPermissions` on a Spring
configuration class, or use Spring Boot's automatic configuration.

## Defining Permission Types

Permissions are defined by extending the `Intent` interface. Extensions of this
interface represent different operations a user may intend to perform, and may
declare whatever properties they need in order to represent the operation. Each
`Intent` instance is associated with a single user, represented as a Spring
`Authentication`, and asserts that access checks for that user to perform the
described operation have been performed. Intent types may represent concrete
operations, such as reading or updating a specific entity, or they may represent
broad categories of operations, such as operations needing a specific role.
Since they are ordinary interfaces, they may be combined as needed.

An intent type may declare any number of parameterless, non-`void` methods,
which are taken to be property getters. A property is optional if its method has
a `default` implementation or returns an `Optional` type; all other properties
are required. A property's name is the same as its method name, except that if
the method name begins with `get` or `is` followed by a capital letter, the
prefix is removed and the capital letter is converted to lower case.

Consider the following intents from a hypothetical document management system:

```java
interface DocumentAccess extends Intent {

    UUID getDocumentId(); // property name is "id"

}

interface DocumentUpdate extends DocumentAccess {}

interface DocumentEdit extends DocumentUpdate {
    
    String getContent();

    Optional<Integer> getChapter();

    default boolean isNewChapter() {
        return getChapter().isEmpty();
    }

}

interface DocumentAuthorOperation extends DocumentAccess {}

interface DocumentDeletion extends DocumentAuthorAction {}

interface DocumentPublication extends DocumentAuthorOperation, DocumentUpdate {

    PublicationStatus getStatus();

}
```

Several of these types, such as `DocumentEdit`, `DocumentDeletion`, and
`DocumentPublication`, represent complete requests. In many cases, it makes
sense to treat the root of an intent hierarchy as simple read access, and if
this application follows this convention, `DocumentAccess` represents a complete
request as well. A `DocumentService` could meaningfully provide methods which
accept only a single parameter of one of these types; this is a recommended
pattern since it ensures access policies have all the information they need to
make a decision.

Other intent types group related operations, such as the `DocumentUpdate` and
`DocumentAuthorAction` examples. Types like these are typically used to attach
an access policy to many kinds of operations. For example, an access policy for
`DocumentUpdate` might check that the user is allowed to update the document and
that the document is not locked, since these requirements make sense for all 
update operations. An application could also create intent types corresponding
to different roles, such as `ModeratorOperation`, to emulate `hasRole`.

## Defining Access Policies

An access policy is simply a Spring bean that implements `AccessPolicy`. Each
access policy applies to some intent type. Policies are covariant, and more
general policies are tested before more specific ones.

Consider the following examples:

```java
@Component
class DocumentAccessPolicy implements AccessPolicy<DocumentAccess> {

    @Override
    public @Nullable Denial apply(DocumentAccess request) {
        var id = request.getDocumentId();
        var user = Intent.getAuthentication(request);
        if (documentExistsAndIsVisibleTo(id, user)) {
            return null;
        } else {
            return () -> new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

}

@Component
class DocumentUpdatePolicy implements AccessPolicy<DocumentUpdate> {

    @Override
    public @Nullable Denial apply(DocumentUpdate request) {
        var id = request.getDocumentId();
        var user = Intent.getAuthentication(request);
        if (!canEdit(id, user)) {
            return () -> new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else if (isLocked(id)) {
            return () -> new ResponseStatusException(HttpStatus.CONFLICT);
        } else {
            return null;
        }
    }

}
```

Since `DocumentUpdate` extends `DocumentAccess`, the `DocumentAccessPolicy` is
applied first. Even if document IDs are sensitive, the `DocumentUpdatePolicy`
can return detailed errors without any risk of confirming that an ID exists.

This example uses the `ResponseStatusException` from Spring Web. Applications
may use a domain-specific exception if they'd prefer, especially if they do not
use Spring Web.

## Creating Intent Objects

An intent object is instantiated from a *source object*, which must be able to
satisfy each of the intent type's required properties. Typically, property
values are retrieved from the source object reflectively. For each property, if
the source object has a public, parameterless method whose name is the same as
the property's name (ignoring any `get` or `it` prefixes on either method), and
the method's return type can be converted to the property's type using Spring's
conversion service, the source object satisfies the property. If the intent type
has any optional properties, they do not need to be satisfied; however, if the
source object has a method which could satisfy an optional property but is not
convertible, the source object is invalid.

The `DocumentUpdate` type in the examples above has two required properties,
`documentId` and `content` of type `UUID` and `String` respectively, and two
optional properties, `chapter` of type `Optional<Integer>` and `newChapter` of
type `boolean`. A valid source object must have a `public` method named
`documentId()` or `getDocumentId()` whose return type is convertible to `UUID`,
and a `content()` or `getContent()` method whose return type is convertible to
`String`. If it has a `chapter()` or `getChapter()` method or a `newChapter()`
or `isNewChapter()` method, they must be convertible to `Optional<Integer>` and
`boolean` respectively, and are used instead of the default values.

If an intent type has exactly one required property, any object which can be
converted to its type is also a valid source object. The `DocumentAccess` type
has one required property, `documentId` of type `UUID`, so a `UUID`, `String`,
or any other type which can be converted to a `UUID` is also a valid source
object.

Reflectively satisfying properties is not appropriate for all source objects,
and the `PropertyExtractor` SPI can be implemented to define other strategies.
For example, if a Jackson `ObjectMapper` bean is available, a
`PropertyExtractor<JsonNode>` is automatically registered.

Intent objects can be created directly by a `StaticPermissionService`, or
converted using Spring's `ConversionService`. If the source and intent types are
known ahead of time, an appropriately typed `IntentFactory` can be autowired;
the autowiring fails if the source type is not valid for the intent type,
providing quick feedback.

package io.github.naomimyselfandi.staticpermissions_integration;

import io.github.naomimyselfandi.staticpermissions.web.MergedIntent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/ing")
class TestController {

    @GetMapping("/foo/{eggs}")
    ResponseEntity<String> test(@MergedIntent(params = "spam") FooIntent intent) {
        return ResponseEntity.ok(intent.toString());
    }

    @GetMapping("/bar/{eggs}")
    ResponseEntity<String> testWithBody(@MergedIntent(body = "spam") FooIntent intent) {
        return test(intent);
    }

    @GetMapping("/baz")
    ResponseEntity<String> testWithUnwrappedBody(@MergedIntent(body = "*") FooIntent intent) {
        return test(intent);
    }

}

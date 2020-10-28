package com.intuit.karate.match;

import com.intuit.karate.data.Json;
import com.intuit.karate.graal.JsEngine;
import static org.junit.jupiter.api.Assertions.*;
import static com.intuit.karate.match.MatchType.*;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pthomas3
 */
class MatchTest {

    static final Logger logger = LoggerFactory.getLogger(MatchTest.class);

    private static final boolean FAILS = true;

    private void match(Object actual, MatchType mt, Object expected) {
        match(actual, mt, expected, false);
    }

    String message;

    private void message(String expected) {
        assertTrue(message != null && message.contains(expected), message);
    }

    private void log() {
        logger.debug("{}", message);
    }

    private void match(Object actual, MatchType mt, Object expected, boolean fails) {
        MatchResult mr = Match.that(actual).is(mt, expected);
        message = mr.message;
        if (!fails) {
            assertTrue(mr.pass, mr.message);
        } else {
            assertFalse(mr.pass);
        }
    }
    
    @Test
    void testApi() {
        assertTrue(Match.that(null).isEqualTo(null).pass);
    }

    @Test
    void testNull() {
        match(null, EQUALS, null);
        match("", EQUALS, null, FAILS);
        message("data types don't match");
        match("", NOT_EQUALS, null);
        match(null, NOT_EQUALS, null, FAILS);
    }

    @Test
    void testBoolean() {
        match(true, EQUALS, true);
        match(false, EQUALS, false);
        match(true, EQUALS, false, FAILS);
        match(true, NOT_EQUALS, false);
        match(true, NOT_EQUALS, true, FAILS);
    }

    @Test
    void testNumber() {
        match(1, EQUALS, 1);
        match(0.1, EQUALS, .100);
        match(100, EQUALS, 200, FAILS);
        match(100, NOT_EQUALS, 2000);
        match(300, NOT_EQUALS, 300, FAILS);
    }

    @Test
    void testBigDecimal() {
        match(new BigDecimal("1000"), EQUALS, 1000);
        match(300, NOT_EQUALS, new BigDecimal("300"), FAILS);
    }

    @Test
    void testString() {
        match("hello", EQUALS, "hello");
        match("foo", EQUALS, "bar", FAILS);
    }

    @Test
    void testStringContains() {
        match("hello", CONTAINS, "hello");
        match("hello", NOT_CONTAINS, "hello", FAILS);
        match("foobar", CONTAINS, "bar");
        match("foobar", CONTAINS, "baz", FAILS);
        match("foobar", NOT_CONTAINS, "baz");
    }

    @Test
    void testBytes() {
        match("hello".getBytes(), EQUALS, "hello".getBytes());
        match("hello".getBytes(), NOT_EQUALS, "helloo".getBytes());
        match("hello".getBytes(), NOT_EQUALS, "hello".getBytes(), FAILS);
    }

    @Test
    void testList() {
        match("[1, 2, 3]", EQUALS, "[1, 2, 3]");
        match("[1, 2, 3]", NOT_EQUALS, "[1, 2, 4]");
        match("[1, 2]", EQUALS, "[1, 2, 4]", FAILS);
        match("[1, 2, 3]", CONTAINS, "[1, 2, 3]");
        match("[1, 2, 3]", CONTAINS_ONLY, "[1, 2, 3]");
        match("[1, 2, 3]", CONTAINS_ONLY, "[3, 2, 1]");
        match("[1, 2, 3]", CONTAINS, "[1, 2, 4]", FAILS);
        match("[1, 2, 3]", NOT_CONTAINS, "[1, 2, 4]");
        match("[1, 2, 3]", CONTAINS_ANY, "[1, 2, 4]");
        match("[{ a: 1 }, { b: 2 }, { c: 3 }]", EQUALS, "[{ a: 1 }, { b: 2 }, { c: 3 }]");
        match("[{ a: 1 }, { b: 2 }, { c: 3 }]", EQUALS, "[{ a: 1 }, { b: 2 }, { c: 4 }]", FAILS);
        match("[{ a: 1 }, { b: 2 }, { c: 3 }]", CONTAINS, "[{ a: 1 }, { b: 2 }, { c: 3 }]");
        match("[{ a: 1 }, { b: 2 }, { c: 3 }]", CONTAINS_ONLY, "[{ a: 1 }, { b: 2 }, { c: 3 }]");
        match("[{ a: 1 }, { b: 2 }, { c: 3 }]", CONTAINS, "[{ a: 1 }, { c: 3 }]");
        match("[{ a: 1 }, { b: 2 }, { c: 3 }]", CONTAINS_ANY, "[{ a: 9 }, { c: 3 }]");
        match("[{ a: 1 }, { b: 2 }, { c: 3 }]", CONTAINS_ANY, "[{ a: 9 }, { c: 9 }]", FAILS);
        match("[{ a: 1 }, { b: 2 }, { c: 3 }]", CONTAINS_DEEP, "[{ a: 1 }, { c: 3 }]");
        match("[{ a: 1 }, { b: [1, 2, 3] }]", CONTAINS_DEEP, "[{ b: [2] }]");
    }
    
    @Test
    void testListContains() {
        match("['foo', 'bar']", CONTAINS, "baz", FAILS);
        message("actual array does not contain expected item - baz");
        match("['foo', 'bar']", CONTAINS, "['baz']", FAILS);
        message("actual array does not contain expected item - baz");
    }

    @Test
    void testEach() {
        match("[1, 2, 3]", EACH_EQUALS, "#number");
        match("[1, 2, 3]", EACH_EQUALS, "#number? _ > 0");
        match("[1, 2, 3]", EACH_EQUALS, "#number? _ < 2", FAILS);
        message("match each failed at index 1");
        match("[1, 'a', 3]", EACH_EQUALS, "#number", FAILS);
        message("$[1] | not a number");
        match("[{ a: 1 }, { a: 2 }]", EACH_EQUALS, "#object");
        match("[{ a: 1 }, { a: 2 }]", EACH_EQUALS, "{ a: '#number' }");
    }
    
    @Test
    void testArray() {
        match("[{ a: 1 }, { a: 2 }]", EQUALS, "#[2]");        
        match("[{ a: 1 }, { a: 2 }]", EQUALS, "#[] #object");
    }
    
    @Test
    void testSchema() {
        Json json = new Json("{ a: '#number' }");
        Map map = json.asMap();
        match("[{ a: 1 }, { a: 2 }]", EACH_EQUALS, map);
        JsEngine.global().put("schema", map);
        match("[{ a: 1 }, { a: 2 }]", EQUALS, "#[] schema");
    }    
    
    @Test
    void testMap() {
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ b: 2, c: 3, a: 1 }");
        match("{ a: 1, b: 2, c: 3 }", CONTAINS, "{ b: 2, c: 3, a: 1 }");
        match("{ a: 1, b: 2, c: 3 }", CONTAINS_ONLY, "{ b: 2, c: 3, a: 1 }");
        match("{ a: 1, b: 2, c: 3 }", CONTAINS_DEEP, "{ c: 3, a: 1 }");
        match("{ a: 1, b: 2, c: [1, 2] }", CONTAINS_DEEP, "{ a: 1, c: [2] }");
        match("{ a: 1, b: 2, c: 3 }", CONTAINS, "{ b: 2 }");
        match("{ a: 1, b: 2, c: 3 }", CONTAINS_ANY, "{ z: 9, b: 2 }");
        match("{ a: 1, b: 2, c: 3 }", CONTAINS, "{ z: 9, x: 2 }", FAILS);
        message("$ | actual does not contain expected | actual does not contain key - 'z'");
        match("{ a: 1, b: 2, c: 3 }", CONTAINS_ANY, "{ z: 9, x: 2 }", FAILS);
        message("$ | actual does not contain expected | no key-values matched");
        message("$.x | data types don't match");
        message("$.z | data types don't match");
    }

    @Test
    void testJsonFailureMessages() {
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ a: 1, b: 9, c: 3 }", FAILS);
        message("$.b | not equal");
        match("{ a: { b: { c: 1 } } }", EQUALS, "{ a: { b: { c: 2 } } }", FAILS);
        message("$.a.b.c | not equal");
    }
    
    @Test
    void testXmlFailureMessages() {
        match("<a><b><c>1</c></b></a>", EQUALS, "<a><b><c>2</c></b></a>", FAILS);
        message("/ | not equal | match failed for name: 'a'");
        message("/a | not equal | match failed for name: 'b'");
        message("/a/b | not equal | match failed for name: 'c'");
        message("/a/b/c | not equal");
        match("<hello foo=\"bar\">world</hello>", EQUALS, "<hello foo=\"baz\">world</hello>", FAILS);
        message("/ | not equal | match failed for name: 'hello'");
        message("/hello/@foo | not equal");
    }    

    @Test
    void testMapFuzzyIgnores() {
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ b: 2, c: 3, z: '#ignore', a: 1 }");
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ b: 2, c: 3, z: '#notpresent', a: 1 }");
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ b: 2, c: 3, z: '##anything', a: 1 }"); // not really correct, TODO !        
    }
    
    @Test
    void testMapFuzzy() {
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ b: 2, c: '#number', a: 1 }");
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ b: 2, c: '#present', a: 1 }");
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ b: 2, c: '#notnull', a: 1 }");
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ b: 2, c: '#null', a: 1 }", FAILS);
        message("$.c | not null");
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ b: 2, c: '#string', a: 1 }", FAILS);
        message("$.c | not a string");
        match("{ a: 1, b: 2, c: 3 }", EQUALS, "{ b: 2, c: '#notpresent', a: 1 }", FAILS);
        message("$.c | present");
        match("{ a: 1, b: 'foo', c: 2 }", EQUALS, "{ b: '#regex foo', c: 2, a: 1 }");
        match("{ a: 1, b: 'foo', c: 2 }", EQUALS, "{ b: '#regex .+', c: 2, a: 1 }");
        match("{ a: 1, b: 'foo', c: 2 }", EQUALS, "{ b: '#regex .{3}', c: 2, a: 1 }");
        match("{ a: 1, b: 'foo', c: 2 }", EQUALS, "{ b: '#regex .{2}', c: 2, a: 1 }", FAILS);
        message("$.b | regex match failed");
    }    

    @Test
    void testXml() {
        match("<root>foo</root>", EQUALS, "<root>foo</root>");
        match("<root>foo</root>", CONTAINS, "<root>foo</root>");
        match("<root>foo</root>", EQUALS, "<root>bar</root>", FAILS);
        match("<root>foo</root>", CONTAINS, "<root>bar</root>", FAILS);
        match("<root><a>1</a><b>2</b></root>", EQUALS, "<root><a>1</a><b>2</b></root>");
        match("<root><a>1</a><b>2</b></root>", EQUALS, "<root><b>2</b><a>1</a></root>");
        match("<root><a>1</a><b>2</b></root>", CONTAINS, "<root><b>2</b><a>1</a></root>");
        match("<root><a>1</a><b>2</b></root>", CONTAINS, "<root><a>1</a><b>9</b></root>", FAILS);
    }
    
    @Test
    void testXmlSchema() {
        match("<root></root>", EQUALS, "<root>#null</root>"); // TODO controversial
        match("<root></root>", EQUALS, "<root>#present</root>");
        match("<root><a>x</a><b><c>y</c></b></root>", EQUALS, "<root><a>#string</a><b><c>#string</c></b></root>");
    }

    @Test
    void testXmlEqualsSchema(){
        match("<root><a>x</a></root>", EQUALS_SCHEMA, "<root><a>#string</a><b><c>#string</c></b></root>");
        match("<root><a>x</a><b></b></root>", EQUALS_SCHEMA, "<root><a>#string</a><b><c>#string</c></b></root>", FAILS);
        match("<root><a>x</a><b><c></c></b></root>", EQUALS_SCHEMA, "<root><a>#string</a><b><c>#string</c></b></root>", FAILS);
        match("<root><a>x</a><b><c>y</c></b></root>", EQUALS_SCHEMA, "<root><a>#string</a><b><c>#string</c></b></root>");
    }

    @Test
    void testJsonEqualsSchema(){
        match("{ a: 'x' }", EQUALS_SCHEMA, "{ a: '#string', b: { c: '#string' }}");
        match("{ a: 'x', b:{} }", EQUALS_SCHEMA, "{ a: '#string', b: { c: '#string' }}");
        match("{ a: 'x', b:{ c: 'y' } }", EQUALS_SCHEMA, "{ a: '#string', b: { c: '#string' }}");
    }


}

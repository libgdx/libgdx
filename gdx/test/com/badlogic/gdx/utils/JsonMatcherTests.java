
package com.badlogic.gdx.utils;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class JsonMatcherTests {
	@Test
	public void singlePatterns () {
		test( // *
			json, //
			"*/(type)", //
			"ENCHARGE"); // First

		test( // @
			json, //
			"*@/(type)", //
			"ENCHARGE", "ENPOWER");

		test( // * for nested paths
			json, //
			"*/devices/*/(serial_num,percentFull)", //
			"{serial_num:32131444,percentFull:100}");

		test( // Multiple wildcards in sequence
			"[{a:[{deep:value}]}]", //
			"*/*/*/(deep)", //
			"value");

		test( // Pattern ending with wildcard (not useful)
			"{root:{a:1,b:2}}", //
			"root/(a)/*", //
			"1");

		test( // ** recursive wildcard
			json, //
			"*/(type)", //
			"ENCHARGE");

		test( // ** to find value at any depth
			json, //
			"*/devices/**/(value)", //
			"1");

		test( // ** recursive wildcard
			"{a:{b:{value:1}},c:{value:2},value:3}", //
			"**/(value)", //
			"1");

		test( // ** followed by literal
			"{servers:{prod:{config:{port:8080}}},config:{port:9090}}", //
			"**/config/(port)", //
			"8080");

		test( // *@ process each array element
			json, //
			"*/devices/*@/(serial_num,percentFull)", //
			"{serial_num:32131444,percentFull:100}", "{percentFull:75,serial_num:234234211}", "{serial_num:9834711}");

		test( // *@ with nested @
			"{outer:[{id:1,inner:[{name:A},{name:B}]}]}", //
			"outer/*@/(id),inner@/*/(name[])", //
			"1", "[A,B]");

		test( // *@ followed by * with multiple matches
			"{data:[{items:[{value:1},{value:2}]}]}", //
			"data/*@/items/*/(value)", //
			"2");

		test( // * followed by *@
			"{data:[{items:[{value:1},{value:2}]}]}", //
			"data/*/items/*@/(value)", //
			"1", "2");

		test( // **@ recursive process
			json, //
			"*/devices/**@/(value)", //
			"1", "2");

		test( // **@ recursive process
			"{id:1,child:{id:2,nested:{id:3}}}", //
			"**@/(id)", //
			"1", "2", "3");

		test( // Literal path after nested object
			"{config:{version:1.0,debug:{level:info},name:MyApp}}", //
			"config/(name)", //
			"MyApp");

		test( // Multiple literal paths at same level
			"{servers:{prod:{host:prod.example.com},dev:{host:dev.example.com}}}", //
			"servers/prod/(host)", //
			"prod.example.com");

		test( // Wildcard collecting from siblings
			"{data:{item1:{nested:{deep:true},id:first},item2:{id:second}}}", //
			"data/*/(id)", //
			"first");

		test( // Empty JSON
			"{}", //
			"test/(value)");

		test( // Empty array
			"[]", //
			"*/(name)");

		test( // Deeply nested structure
			"{a:{b:{c:{d:{e:{f:{value:deep}}}}}}}", //
			"a/b/c/d/e/f/(value)", //
			"deep");

		test( // Array of arrays
			"{data:[[{id:1},{id:2}],[{id:3}]]}", //
			"data/*/*/(id)", //
			"1");

		test( // Object with no fields,process each
			"{empty:[{},{}]}", //
			"empty/(*@)", //
			"{}", "{}");

		test( // Object with no fields,as array
			"{empty:[{},{}]}", //
			"empty/(*)", //
			"{}"); // First

		test( // Literal path no match
			"{users:{johnny:{age:30}}}", //
			"users/john/(hair)");

		test( // Literal path no match
			"{users:{johnny:{age:30}}}", //
			"users/john/*/(age)");

		test( // Test dead node,no match
			"{data:{item1:{nested:{deep:true},id:first},item2:{id:second}}}", //
			"data/(deep)");

		test( // Pattern that matches nothing
			"{other:{path:{value:test}}}", //
			"nonexistent/path/(value)");

		test( // Same field name at different levels
			"{outer:{name:A,inner:{name:B}}}", //
			"outer/(name),inner/(name)", //
			"{name:B}");

		test( // Field names with special characters
			"{data:{field.with.dots:1,field-with-dashes:2,field_with_underscores:3}}", //
			"data/(field.with.dots,field-with-dashes,field_with_underscores)", //
			"{field.with.dots:1,field-with-dashes:2,field_with_underscores:3}");

		test( // Collect array items
			"{data:{z:1,x:{ok:true},items:[a,b,c]}}", //
			"data/items/(*@)", //
			"a", "b", "c");

		test( // Collect array itself
			"{data:{z:1,x:{ok:true},items:[a,b,c]}}", //
			"data/(items)", //
			"[a,b,c]");

		test( // Collect array itself with []
			"{data:{items:[a,b,c]}}", //
			"data/(items[])", //
			"[[a,b,c]]");

		test( // Collect empty array
			"{data:{empty:[]}}", //
			"data/(empty)", //
			"[]");

		test( // *@ with array collection
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"items/(*@)", //
			"{tags:[red,big]}", "{tags:[blue]}");

		test( // *@ with array collection by name
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"items/*@/(tags)", //
			"[red,big]", "[blue]");

		test( // * for whole array
			json, //
			"*/devices/*/(device_status)", //
			"[envoy.global.ok,prop.done]"); // 1 match because processing ends

		test( // *@ for whole arrays
			json, //
			"*/devices/*@/(device_status)", //
			"[envoy.global.ok,prop.done]", "[envoy.global.failure,prop.waiting]");

		test( // (*) for single array values
			json, //
			"*/devices/*/device_status/(*)", //
			"envoy.global.ok"); // First

		test( // (*) for array values
			json, //
			"*/devices/*/device_status/(*[])", //
			"[envoy.global.ok,prop.done,envoy.global.failure,prop.waiting]");

		test( // *@ for array values
			json, //
			"*/devices/*/device_status/(*@)", //
			"envoy.global.ok", "prop.done", "envoy.global.failure", "prop.waiting");

		test( // * and [] with array collection
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"items/*/(tags[])", //
			"[[red,big],[blue]]");

		test( // **@ with array collection
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"**@/(tags)", //
			"[red,big]", "[blue]");

		test( // * with array collection
			"{items:[{tags:[{a:red},{b:big}]},{tags:[{c:blue}]}]}", //
			"items/*/(tags[])", //
			"[[{a:red},{b:big}],[{c:blue}]]");

		test( // **@ with object collection
			"{items:[{tags:[{a:red},{b:big}]},{tags:[{c:blue}]}]}", //
			"**@/(tags)", //
			"[{a:red},{b:big}]", "[{c:blue}]");

		test( // ** before *@ for per array item processing
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"**/tags/(*@)", //
			"red", "big", "blue");

		test( // */* and process each
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"*/*/(*@)", //
			"[red,big]", "[blue]");

		test( // process each at root array
			"[{tags:[red,big]},{tags:[blue]}]", //
			"(*@)", //
			"{tags:[red,big]}", "{tags:[blue]}");

		test( // process each at root object
			"{tags1:[red,big],tags2:[blue]}", //
			"(*@)", //
			"[red,big]", "[blue]");

		test( // process each at root object
			"{items1:{tags:[red,big]},items2:{tags:[blue]}}", //
			"(*@)", //
			"{tags:[red,big]}", "{tags:[blue]}");

		test( // Multiple array collections
			"{data:{a:[1,2],b:[3,4],c:[5,6]}}", //
			"data/(a,b,c)", //
			"{a:[1,2],b:[3,4],c:[5,6]}");

		test( // Mix regular and array collection
			"{a:{x:1,y:2},b:{x:3,y:4}}", //
			"*/(x,y[])", //
			"{x:3,y:[2,4]}");

		test( // **@ with array collection
			"{a:{val:1},b:{c:{val:2},d:{val:3}}}", //
			"**@/(val[])", //
			"[1]", "[2]", "[3]");

		test( // Deep recursive wildcard
			"{a:{a:{a:{a:{a:{x:deep}}}}}}", //
			"**/(x)", //
			"deep");

		test( // *@ followed by **@
			"{items:[{deep:{a:{x:1},b:{x:2}}}]}", //
			"items/*@/deep/**@/(x)", //
			"1", "2");

		test( // ** at root level
			"{x:1,a:{x:2},b:{c:{x:3}}}", //
			"**/(x[])", //
			"[1,2,3]");

		test( // *@ on primitive array
			"{tags:[red,blue,green]}", //
			"tags/(*@)", //
			"red", "blue", "green");

		test( // Nested arrays with *@
			"{matrix:[[1,2],[3,4]]}", //
			"matrix/*/(*@)", //
			"1", "2", "3", "4");

		test( // *@ with multiple fields
			"[{id:1,name:A},{id:2,name:B}]", //
			"*@/(id,name)", //
			"{id:1,name:A}", "{id:2,name:B}");

		test( // ** followed by literal
			"{a:{b:{target:{x:1}},c:{target:{x:2}}}}", //
			"**/target/(x[])", //
			"[1,2]");

		test( // *@ for nested object values
			"{data:{a:{val:1},b:{val:2},c:{val:3}}}", //
			"data/*/(*@)", //
			"1", "2", "3");

		test( // Escaped quotes
			"{data:'He said \\\"hello\\\"'}", //
			"(data)", //
			"'He said \"hello\"'");

		test( // Multiple *@ wildcards
			"{list:[{items:[a,b]},{items:[c,d]}]}", //
			"list/*@/items/(*@)", //
			"a", "b", "c", "d");

		test( // * and ** combination
			"{a:{b:{c:{d:1}}},x:{b:{c:{d:2}}}}", //
			"*/b/**/(d)", //
			"1");

		test( // * and ** combination,match first
			"{a:{b:{c:{d:1}}},x:{b:{c:{d:2}}}}", //
			"*/b/**/(c)", //
			"{d:1}");

		test( // * and ** combination,as array
			"{a:{b:{c:{d:1}}},x:{b:{c:{d:2}}}}", //
			"*/b/**/(c[])", //
			"[{d:1},{d:2}]");

		test( // Null value
			"{data:{name:null,value:123}}", //
			"data/(name,value)", //
			"{name:null,value:123}");

		test( // Null in array
			"{items:[null,a,null,b]}", //
			"items/(*@)", //
			(String)null, "a", null, "b");

		test( // Null with array collection
			"{data:[{x:null},{x:1},{x:null}]}", //
			"data/*/(x[])", //
			"[null,1,null]");

		test( // Boolean and numeric values
			"{config:{debug:true,port:8080,ratio:3.14}}", //
			"config/(debug,port,ratio)", //
			"{debug:true,port:8080,ratio:3.14}");

		test( // Empty field name
			"{\"\":empty,normal:value}", //
			"('')", //
			"empty");

		test( // Empty field name
			"{\"\":empty,normal:value}", //
			"('',normal)", //
			"{\"\":empty,normal:value}");

		test( // Unicode in field names
			"{user:{名前:太郎,età:25,émoji:🚀}}", //
			"user/(名前,età,émoji)", //
			"{名前:太郎,età:25,émoji:🚀}");

		test( // Very deeply nested (10 levels)
			"{a:{b:{c:{d:{e:{f:{g:{h:{i:{j:{value:deep}}}}}}}}}}}", //
			"a/b/c/d/e/f/g/h/i/j/(value)", //
			"deep");

		test( // ** matching at multiple depths
			"{a:{x:1,b:{x:2,c:{x:3}}}}", //
			"**/(x)", //
			"1");

		test( // Multiple ** in pattern
			"{a:{b:{c:{d:{e:value}}}}}", //
			"**/b/**/d/(e)", //
			"value");

		test( // *@ at multiple levels simultaneously
			"{data:[{items:[1,2]},{items:[3,4]}]}", //
			"data/*@/items/(*@)", //
			"1", "2", "3", "4");

		test( // Mixed object and array wildcards
			"{data:[{a:1},{b:2}],meta:{items:[{c:3},{d:4}]}}", //
			"*/*/*@/(*)", //
			"3", "4");

		test( // Mixed object and array wildcards,as arrays
			"{data:[{a:1},{b:2}],meta:{items:[{c:3},{d:4,e:5}]}}", //
			"*/*/*@/(*[])", //
			"[3]", "[4,5]");

		test( // Numeric-like field names
			"{\"123\":{\"456\":value,normal:other}}", //
			"123/(456,normal)", //
			"{456:value,normal:other}");

		test( // Whitespace in pattern
			"{\"  field \n \":{\" \t nested \t\":value}}", //
			"  field \n /( \t nested \t)", //
			"value");

		test( // Whitespace in pattern
			"{\"  field \n \":{\" \t nested \t\":value}}", //
			"(  field \n )", //
			"{\" \\t nested \\t\":value}");

		test( // **@ matching objects at all levels
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**@/(*)", //
			"1", "{c:2,d:{e:3}}");

		test( // Complex array of arrays of objects
			"{data:[[[{x:1}]],[[{x:2}],[{x:3}]]]}", //
			"data/*/*/*/(x)", //
			"1");

		test( // Complex array of arrays of objects []
			"{data:[[[{x:1}]],[[{x:2}],[{x:3}]]]}", //
			"data/*/*/*/(x[])", //
			"[1,2,3]");

		test( // *@ on empty array
			"{empty:[]}", //
			"empty/(*@)");

		test( // (*) on empty object
			"{empty:{}}", //
			"empty/(*)");

		test( // Multiple patterns matching same field
			"{user:{profile:{name:John,age:30}}}", //
			"user/profile/(name)", //
			"John");

		test( // Multiple patterns matching same field []
			"{user:{profile:{name:John,age:30}}}", //
			"user/profile/(name[])", //
			"[John]");

		test( // **@ with nested matches
			"{a:{val:1,b:{val:2,c:{val:3}}}}", //
			"a/**@/(val)", //
			"1", "2", "3");

		test( // Very large array
			"{items:[" + "1,".repeat(99) + "100]}", //
			"items/(*[])", //
			"[" + "1,".repeat(99) + "100]");

		test( // *@ followed by literal path
			"{list:[{user:{name:A}},{user:{name:B}}]}", //
			"list/*@/user/(name)", //
			"A", "B");

		test( // ** followed by @
			"{deep:{nested:{items:[a,b,c]}}}", //
			"**/items/(*@)", //
			"a", "b", "c");

		test( // Collecting same field at different depths
			"{name:root,child:{name:nested}}", //
			"(name),child/(name)", //
			"{name:nested}");

		test( // ** with array collection across levels
			"{a:{x:[1,2]},b:{c:{x:[3,4]}}}", //
			"**/(x[])", //
			"[[1,2],[3,4]]");

		test( // Empty object at root
			"{}", //
			"(*)");

		test( // *@ on object
			"{users:{john:{age:30},jane:{age:25}}}", //
			"users/*@/(age)", //
			"30", "25");

		test( // Multiple *@ in different branches
			"{left:[{x:1},{x:2}],right:[{x:3},{x:4}]}", //
			"*/*@/(x)", //
			"1", "2", "3", "4");

		test( // Pattern matching primitive at root
			"42", //
			"(value)");

		test( // String at root level
			"\"hello\"", //
			"(*)", //
			"hello");

		test( // **@ starting from non-root
			"{skip:{this:{find:{me:{x:1}}}}}", //
			"skip/**/find/**@/(x)", //
			"1");

		test( // Collecting fields with special JSON characters
			"{\"field\nwith\nnewlines\":1,\"field\twith\ttabs\":2}", //
			"(field\nwith\nnewlines,field\twith\ttabs)", //
			"{field\\nwith\\nnewlines:1,field\\twith\\ttabs:2}");

		test( // *@ with mixed types in array
			"{mixed:[1,string,true,null,{obj:value}]}", //
			"mixed/(*@)", //
			"1", "string", "true", null, "{obj:value}");

		test( // Overlapping **@ and ** patterns
			"{a:{b:{c:{x:1,d:{x:2}}}}}", //
			"**@/b/**/(x),d/(x)", //
			"1", "2");

		test( // Overlapping **@ patterns
			"{a:{b:{c:{x:1,d:{x:2}}}}}", //
			"**@/b/**@/(x),d/(x)", //
			"1", "2");

		test( // Overlapping **@ and ** patterns,test pop with multiple branches
			"[{a:{b:{x:1,bb:{c:{x:2}}}}},{a:{b:{x:3,bb:{c:{x:4}}}}}]", //
			"**@/b/(x),*/**/c/(x)", //
			"1", "2", "3", "4");

		test( // Overlapping ** and **@ patterns
			"{a:{b:{x:1,bb:{c:{x:2,d:{x:3}}}}}}", //
			"**/b/(x),bb/**@/c/(x),d/(x)", //
			"1", "2", "3");

		test( // (*) with nested objects
			"{data:{a:{deep:1},b:{deep:2}}}", //
			"(*)", //
			"{a:{deep:1},b:{deep:2}}");

		test( // (*) with nested objects
			"{data:{a:{deep:1},b:{deep:2}}}", //
			"data/(*)", //
			"{deep:1}");

		test( // (*) with nested objects []
			"{data:{a:{deep:1},b:{deep:2}}}", //
			"data/(*[])", //
			"[{deep:1},{deep:2}]");

		test( // (*@) with nested objects
			"{data:{a:{deep:1},b:{deep:2}}}", //
			"data/(*@)", //
			"{deep:1}", "{deep:2}");

		test( // *@ collecting entire objects
			"{list:[{id:1,data:{x:10}},{id:2,data:{x:20}}]}", //
			"list/*@/(data)", //
			"{x:10}", "{x:20}");

		test( // Pattern with only wildcards
			"{a:{b:{c:value}}}", //
			"*/*/(c)", //
			"value");

		test( // **@ at root collecting arrays
			"{items:[1,2,3]}", //
			"**@/(items)", //
			"[1,2,3]");

		test( // Complex nested with multiple array depths
			"{root:[{level1:[{level2:[{value:deep}]}]}]}", //
			"root/*@/level1/*@/level2/*@/(value)", //
			"deep");

		test( // *@ on array of primitives and objects mixed
			"{items:[1,{x:2},3,{x:4}]}", //
			"items/*@/(x)", //
			"2", "4");

		test( // Collecting nothing from existing structure
			"{a:{b:{c:1}},d:{e:{f:2}}}", //
			"x/y/(z)");

		test( // ** with literal after multiple levels
			"{a:{b:{target:miss},c:{d:{target:{hit:true}}}}}", //
			"**/d/target/(hit)", //
			"true");

		test( // Array index-like field names
			"{\"0\":first,\"1\":second,\"2\":third}", //
			"(0,1,2)", //
			"{0:first,1:second,2:third}");

		test( // *@ with empty field collection
			"{items:[{},{},{}]}", //
			"items/*@/(*)");

		test( // Very long field name
			"{\"" + "x".repeat(100) + "\":value}", //
			"(" + "x".repeat(100) + ")", //
			"value");

		test( // **@ with *@ combination
			"{level1:[{level2:[{x:1},{x:2}]}]}", //
			"**@/level2/*@/(x)", //
			"1", "2");

		test( // Object with array-like structure
			"{data:{\"length\":3,\"0\":a,\"1\":b,\"2\":c}}", //
			"data/(length,0,1,2)", //
			"{length:3,0:a,1:b,2:c}");

		test( // Escaped characters in values
			"{data:{value:\"line1\\nline2\\ttab\"}}", //
			"data/(value)", //
			"line1\nline2\ttab");

		test( // *@ at root with nested *
			"[{a:{b:[1,2]}},{a:{b:[3,4]}}]", //
			"*@/a/b/(*)", //
			"2", "4"); // Only last

		test( // *@ at root with *[]
			"[{a:{b:[1,2]}},{a:{b:[3,4]}}]", //
			"*@/a/b/(*[])", //
			"[1,2]", "[3,4]");

		test( // Multiple array collections with overlap
			"{x:[1,2],y:[2,3],z:[3,4]}", //
			"(x,y,z)", //
			"{x:[1,2],y:[2,3],z:[3,4]}");

		test( // ** collecting all with *
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**/(*)", //
			"1");

		test( // ** collecting all with *[]
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**/(*[])", //
			"[1,{c:2,d:{e:3}}]");

		test( // ** collecting all with *@
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**/(*@)", //
			"1", "{c:2,d:{e:3}}");

		test( // **@ collecting all with *
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**@/(*)", //
			"1", "{c:2,d:{e:3}}");

		test( // **@ collecting all with *@
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**@/(*@)", //
			"1", "{c:2,d:{e:3}}");

		test( // Same field names at different levels
			"{node:{value:1,node:{value:2,node:{value:3}}}}", //
			"**/node/(value)", //
			"1");

		test( // Multiple fields with same prefix
			"{user:1,userName:2,userAge:3}", //
			"(user,userName,userAge)", //
			"{user:1,userName:2,userAge:3}");

		test( // Pattern matching boolean false
			"{flags:{active:false,debug:false,enabled:true}}", //
			"flags/(active,debug,enabled)", //
			"{active:false,debug:false,enabled:true}");

		test( // Multiple patterns capturing same field name
			"{user:{name:John},profile:{name:Jane}}", //
			"user/(name)", // This should end after first match
			"John");

		test( // Field name appears at multiple depths
			"{x:1,nested:{x:2,deep:{x:3}}}", //
			"(x),nested/(x)", //
			"{x:2}");

		test( // Same field captured through different paths
			"{data:{id:1,user:{id:2}}}", //
			"data/(id),user/(id)", //
			"{id:2}");

		test( // Pattern that could match same field twice
			"{a:{x:1},b:{x:2}}", //
			"*/(x)", //
			"1"); // First

		test( // Overwriting with non-array collection
			"{items:[{x:1},{x:2},{x:3}]}", //
			"items/*/(x)", //
			"1"); // First

		test( // Complex overwrite scenario
			"{level1:{name:A,level2:{name:B}},other:{name:C}}", //
			"*/(name)", //
			"A"); // First

		test( // Capture 2 values with same field name with array collection
			"{first:{x:1,second:{x:2}}}", //
			"first/(x[]),second/(x[])", //
			"{x:[1,2]}");

		test( // Multiple wildcards matching same structure
			"{data:{a:{id:1},b:{id:2}}}", //
			"data/*/(id)", //
			"1"); // First

		test( // Multiple wildcards matching same structure with array collection
			"{data:{a:{id:1},b:{id:2}}}", //
			"data/*/(id[])", //
			"[1,2]"); // First

		test( // Same field many levels with array collection
			"{items:[{x:1},{x:2},{x:3}]}", //
			"items/*/(x[])", //
			"[1,2,3]");

		test( // Multiple values where some repeat
			"{data:{x:1,y:2}}", //
			"data/(x,y,x)", //
			"{x:1,y:2}");

		test( // Multiple values where some are missing
			"{data:{x:1,y:2}}", //
			"data/(x,y,z,a,b,c[])", //
			"{x:1,y:2}");

		test( // Many **
			"{data:{x:1,y:2}}", //
			"**/**/**/data/**/**/**/(x,y,x)", //
			"{x:1,y:2}");

		test( // Test dead node being revived for a deeper branch elsewhere
			"{\n" // @off
			+ "	items: {\n"
			+ "		server1: {\n"
			+ "			config: { // dead here\n"
			+ "				host: [ deadend ]\n"
			+ "			},\n"
			+ "			nested: {\n"
			+ "				config: {\n"
			+ "					port: 8080\n"
			+ "				}\n"
			+ "			}\n"
			+ "		}\n"
			+ "	}\n"
			+ "}", // @on
			"items/**/config/(port)", //
			"8080");

		test( // Test multiple [] captures
			"{data1:[{a:1},{b:2},{a:3},{b:4}],data2:[{a:5},{b:6},{a:7},{b:8}]}", //
			"*/*/(a[],b[])", //
			"{a:[1,3,5,7],b:[2,4,6,8]}");
	}

	@Test
	public void wholeDocument () {
		test( // Object
			"{data:{items:[a,b,c]}}", //
			"", //
			"{data:{items:[a,b,c]}}");

		test( // Array
			"[a,b,{data:[1,2,3]},c]", //
			"", //
			"[a,b,{data:[1,2,3]},c]");

		test( // String
			"string", //
			"", //
			"string");

		test( // Long
			"1234567", //
			"", //
			"1234567");

		test( // Double
			"1234.567", //
			"", //
			"1234.567");

		test( // true
			"true", //
			"", //
			"true");

		test( // false
			"false", //
			"", //
			"false");

		test( // null
			"null", //
			"", //
			(String)null);
	}

	@Test
	public void unescaping () {
		test( // Escaped quotes
			"{data:\"He said \\\"hello\\\"\"}", //
			"(data)", //
			"He said \"hello\"");

		test( // Escaped backslash
			"{path:\"C:\\\\Users\\\\file.txt\"}", //
			"(path)", //
			"C:\\Users\\file.txt");

		test( // Escaped newline
			"{text:\"Line 1\\nLine 2\\nLine 3\"}", //
			"(text)", //
			"Line 1\nLine 2\nLine 3");

		test( // Escaped tab
			"{data:\"Column1\\tColumn2\\tColumn3\"}", //
			"(data)", //
			"Column1\tColumn2\tColumn3");

		test( // Escaped carriage return
			"{text:\"Windows\\r\\nLine ending\"}", //
			"(text)", //
			"Windows\r\nLine ending");

		test( // Escaped forward slash
			"{url:\"https:\\/\\/example.com\\/path\"}", //
			"(url)", //
			"https://example.com/path");

		test( // Escaped backspace
			"{data:\"Before\\bAfter\"}", //
			"(data)", //
			"Before\bAfter");

		test( // Escaped form feed
			"{data:\"Page1\\fPage2\"}", //
			"(data)", //
			"Page1\fPage2");

		test( // Unicode escape sequences
			"{emoji:\"\\u2764\\uFE0F\"}", // Heart emoji
			"(emoji)", //
			"❤️");

		test( // Unicode escape for special chars
			"{text:\"\\u00A9 2024 Company\"}", // Copyright symbol
			"(text)", //
			"© 2024 Company");

		test( // Multiple escapes in one string
			"{complex:\"Line 1\\n\\tIndented\\n\\\"Quoted\\\"\\nC:\\\\path\"}", //
			"(complex)", //
			"Line 1\n\tIndented\n\"Quoted\"\nC:\\path");

		test( // Escaped field names
			"{\"field\nwith\nnewlines\":\"value1\",\"field\twith\ttab\":\"value2\"}", //
			"(field\nwith\nnewlines,field\twith\ttab)", //
			"{field\\nwith\\nnewlines:value1,field\\twith\\ttab:value2}");

		test( // Mix of unicode and regular escapes
			"{mixed:\"\\u0048ello\\nWorld\\t\\u0021\"}", // H and !
			"(mixed)", //
			"Hello\nWorld\t!");

		test( // Empty string with escapes
			"{empty:\"\",\"escaped\":\"\n\t\"}", //
			"(empty,escaped)", //
			"{empty:\"\",escaped:\\n\\t}");

		test( // Nested objects with escaped values
			"{user:{\"name\":\"John \\\"Johnny\\\" Doe\",\"bio\":\"Line 1\\nLine 2\"}}", //
			"user/(name,bio)", //
			"{name:John \"Johnny\" Doe,bio:Line 1\\nLine 2}");

		test( // Array with escaped values
			"{items:[\"\\\"quoted\\\"\",\"\ttabbed\",\"new\nline\"]}", //
			"items/(*@)", //
			"\"quoted\"", "\ttabbed", "new\nline");

		test( // Unicode surrogate pairs
			"{emoji:\"\\uD83D\\uDE00\"}", // 😀
			"(emoji)", //
			"😀");

		test( // All escape sequences
			"{all:\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t\"}", //
			"(all)", //
			"\" \\ / \b \f \n \r \t");

		test( // Escaped at different levels
			"{level1:{escaped:\"\\\"value\\\"\"},level2:[{item:\"\\nitem\\n\"}]}", //
			"**/(escaped)", //
			"\"value\"");

		test( // Multiple unicode in sequence
			"{text:\"\\u0041\\u0042\\u0043\"}", // ABC
			"(text)", //
			"ABC");

		test( // Multiple unicode in sequence,unquoted
			"{text:\\u0041\\u0042\\u0043}", // ABC
			"(text)", //
			"ABC");

		test( // Unicode with array processing
			"{names:[\"\\u4E2D\\u6587\",\"\\u65E5\\u672C\\u8A9E\"]}", // Chinese,Japanese
			"names/(*@)", //
			"中文", "日本語");

		test( // Very long escaped string
			"{long:\"" + "\\n".repeat(10) + "\"}", //
			"(long)", //
			"\n".repeat(10));

		test( // Escaped within wildcards
			"{data:{\"field\\nname\":\"value\\there\"}}", //
			"data/(*)", //
			"value\there");

		test( // *@ with escaped array values
			"{list:[{text:\"\\\"A\\\"\",value:1},{text:\"\\\"B\\\"\",value:2}]}", //
			"list/*@/(text,value)", //
			"{text:\"\\\"A\\\"\",value:1}", "{text:\"\\\"B\\\"\",value:2}");

		test( // ** then *
			json, //
			"**/*/(serial_num)", //
			"32131444");

		test( // *@ with objects containing @
			"{items:[{\"@\":special},{normal:value}]}", //
			"items/*@/('@',normal)", //
			"{@:special}", "{normal:value}");

		test( // Escaping inside ''
			"{da\\\\ta:{it'ems:[a,b,c]}}", //
			"'da\\\\ta'/('it''ems')", //
			"[a,b,c]");

		test( // Special characters inside ''
			"{*/()[\\\\]@',\\\\\\\\:{items:[a,b,c]}}", //
			"'*/()[\\\\]@'',\\\\\\\\'/(items)", //
			"[a,b,c]");
	}

	@Test
	public void multiplePatterns () {
		test( // Multiple patterns
			"{user:{name:John,age:30},meta:{version:1.0}}", new String[] { //
				"user/(name)", //
				"meta/(version)"},
			"John", "1.0");

		test( // Same field name,different patterns
			"{user:{name:John},profile:{user:{name:Jane}}}", new String[] { //
				"user/(name)", //
				"profile/user/(name)"},
			"John", "Jane");

		test( // Same field name,same patterns
			"{user:{name:John},profile:{user:{name:Jane}}}", new String[] { //
				"user/(name)", //
				"user/(name)", //
				"profile/user/(name)", //
				"profile/user/(name)"},
			"John", "John", "Jane", "Jane");

		test( // Overlapping matches
			"{a:{b:{c:1}}}", new String[] { //
				"**@/(b)", //
				"**@/(c)"},
			"1", "{c:1}");

		test( // Test capturing only the first value when other pattern prevents stopping
			json, //
			new String[] {"*/(type)", "*/devices/*/(serial_num[])"}, //
			new String[] {"ENCHARGE", "[32131444,234234211,9834711]"});
	}

	@Test
	public void keys () {
		test( // []
			"{a:1,b:2,c:3}", //
			"()[]", //
			"[a,b,c]");

		test( // [] with other capture (doesn't match in captured value)
			"{a:1,b:2,c:3}", //
			"()[],(b)", //
			"{\"\":[a,b,c]}");

		test( // [] with other capture (doesn't match in captured value)
			"{a:1,b:2,c:3}", //
			"()[],(b)", //
			"{\"\":[a,b,c]}");

		test( // Nested []
			"{object:{a:1,b:2,c:3}}", //
			"object/()[]", //
			"[a,b,c]");

		test( //
			"{a:1,b:2,c:3}", //
			"()", //
			"a"); // First

		test( // Nested
			"{object:{a:1,b:2,c:3}}", //
			"object/()", //
			"a"); // First

		test( // Array values (doesn't have keys)
			"[a,b,c]", //
			"()[]");

		test( // * and []
			"{object:{a:1,b:2,c:3}}", //
			"*/()[]", //
			"[a,b,c]");

		test( // ** and []
			"{object:{a:1,b:2,c:{d:{e:3},f:[1,2,3]}}}", //
			"**/()[]", //
			"[object,a,b,c,d,e,f]"); // Array values have no keys
	}

	@Test
	public void earlyEnd () {
		test("extra", // Early end
			"{first:{id:1},second:{data:ignored},extra:should-not-parse}", new String[] { //
				"first/(id@)", //
				"(second)"},
			"1", "{data:ignored}");

		test("extra", // Same name twice
			"{first:{id:1},second:{id:ignored},extra:should-not-parse}", new String[] { //
				"*/(id@)", //
				"*/(id)"},
			"1", "1");

		test("extra", // Multiple values with @ on both
			"{first:{id:1},second:{id:ignored},extra:should-not-parse}", new String[] { //
				"(first,second)@"},
			"{id:1}", "{id:ignored}");

		test("extra", //
			"{first:{id:1},second:{id:ignored},extra:should-not-parse}", new String[] { //
				"second/(id@)"},
			"ignored");

		test("extra", // Multiple values
			"{first:{id:1},second:{id:ignored},third:{other:1},extra:should-not-parse}", new String[] { //
				"(first,second)@", //
				"*/(other)"},
			"{id:1}", "{id:ignored}", "1");

		test("extra", // Same field at different levels
			"{value:1,nested:{value:2},extra:{value:3}}", new String[] { //
				"(value@),*/(value@)"},
			"1", "2");
	}

	@Test
	public void rejection () {
		{ // Reject all except 1.
			Array<JsonValue> values = new Array();
			JsonMatcher matcher = new JsonMatcher();
			matcher.addPattern("*/(type@)", value -> {
				if (value.equalsString("ENCHARGE")) matcher.rejectAll();
			});
			matcher.addPattern("*/devices/*@/(serial_num,percentFull)", value -> copy(value, values));
			matcher.parse(json);

			assertValueCount(1, values);
			JsonValue value = values.first();
			assertEquals("9834711", value.getString("serial_num"));
			assertNull(value.get("percentFull"));
		}

		rejectAll(json, "(type@)", "**/*/*@/(serial_num)");
		rejectAll(json, "**/(serial_num@)", "**/*@/(serial_num[])");
		rejectAll(json, "**/*/(serial_num@)", "**/*@/(serial_num[])");
		rejectAll(json, "**/*/**/(serial_num@)", "**/*@/(serial_num[])");
		rejectAll(json, "**/*/(serial_num@)", "**/(serial_num[])@");
		rejectAll(json, "**/*/(serial_num@)", "**/*/*@/(serial_num)");
		rejectAll(json, "**/*/(part_num@)", "**/*/*@/(serial_num)");
		rejectAll(json, "**/*/(object@)", "**/*@/*/(serial_num)");

		rejectAll( // Rejection when ** needs to backtrack:
			"{a:{x:{reject:true}},b:{x:{value:found}}}", //
			"**/x/(reject@)", //
			"**/x/(value@)", //
			"found");

		rejectAll( // Reject at depth 2, but continue matching at depth 1
			"{items:[{bad:{reject:true}},{good:{value:1}}]}", //
			"items/*@/bad/(reject)", // Reject when finding bad
			"items/*@/good/(value)", // Should still find good in next item
			"1");

		rejectAll( // Rejection mid-array processing
			"{items:[{id:1},{id:2,skip:true},{id:3}]}", //
			"items/*/(skip@)", //
			"items/*@/(id,skip)", //
			"{id:1}", "{id:3}" //
		);

		rejectAll( // Rejection with nested ** patterns:
			"{a:{b:{reject:here,c:{d:{value:no}}},e:{c:{d:{value:yes}}}}}", //
			"**/b/(reject@)", //
			"**/c/**/d/(value)", // Should only find in e branch
			"yes");

		rejectAll( // One pattern rejects, other continues
			"{data:{type:[bad],info:important}}", //
			"data/type@/(*)", //
			"data@/(info)", // Should still capture
			"important");

		rejectAll( // Reject after collecting some values
			"{a:{b:{target:{x:1}},c:{target:{x:2,reject:true}}},d:{target:{x:3}}}", //
			"**/(reject@)", //
			"**/(x[])", //
			"[3]"); // rejectAll removes first 2 matches
	}

	static void rejectAll (String json, String rejectPattern, String pattern, String... expected) {
		Array<JsonValue> values = new Array();
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern(rejectPattern, value -> {
			matcher.rejectAll();
			matcher.clearAll();
		});
		matcher.addPattern(pattern, value -> copy(value, values));
		matcher.parse(json);

		try {
			assertValueCount(expected.length, values);
			for (int i = 0, n = expected.length; i < n; i++)
				assertEquals("Pattern " + i, expected[i], values.get(i).toJson(OutputType.minimal));
		} catch (AssertionError ex) {
			printResults(matcher, values, json, new String[] {rejectPattern, pattern}, expected);
			throw ex;
		}
	}

	@Test
	public void explicitEnd () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("*/(type@)", value -> {
			if (value.equalsString("ENCHARGE")) matcher.end(); // End parsing before any matches.
		});
		Array<JsonValue> values = new Array();
		matcher.addPattern("*/devices/*@/(serial_num,percentFull)", value -> copy(value, values));
		matcher.parse(json);

		assertValueCount(0, values);
	}

	@Test
	public void explicitStop () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("*/(type@)", value -> {
			if (value.equalsString("ENCHARGE")) matcher.stop(); // Stop parsing before any matches.
		});
		Array<JsonValue> values = new Array();
		matcher.addPattern("*/devices/*@/(serial_num,percentFull)", value -> copy(value, values));
		matcher.parse(json);

		assertValueCount(0, values);
	}

	@Test
	public void parseValue () {
		JsonValue root = new JsonMatcher().parseValue(json);
		assertTrue(root.child.hasChild("devices"));

		root = new JsonMatcher("").parseValue(json);
		assertTrue(root.child.hasChild("devices"));

		root = new JsonMatcher("*/devices/(*)").parseValue(json);
		assertEquals(100, root.getInt("percentFull"));

		JsonValue values = new JsonMatcher("*/(devices)", "*/devices/(*)").parseValue(json);
		assertEquals("devices", values.child.name);
		assertEquals(100, values.child.next.getInt("percentFull")); // First even though other pattern prevents stopping
	}

	@Test
	public void paths () {
		Array paths = new Array();
		Array parents = new Array();
		Array parents2 = new Array();
		{
			JsonMatcher matcher = new JsonMatcher();
			matcher.setProcessor(value -> {
				paths.add(matcher.path());
				parents.add(matcher.parent());
				parents2.add(matcher.parent(2));
			});
			matcher.addPattern("*/devices/*@/(serial_num,percentFull)");
			matcher.parse(json);
		}
		{
			JsonMatcher matcher = new JsonMatcher();
			matcher.addPattern("**@/(value)", value -> {
				paths.add(matcher.path());
				parents.add(matcher.parent());
				parents2.add(matcher.parent(2));
			});
			matcher.parse(json);
			matcher.parse("{a:{b:{c:{d:{e:{f:{value:deep}}}}}}}");
		}

		assertValueCount(6, paths);

		assertEquals("[]/{}/devices/{}", paths.first());
		assertEquals("[]/{}/devices/{}", paths.first());
		assertEquals("[]/{}/devices/{}", paths.get(1));
		assertEquals("[]/{}/devices/{}", paths.get(2));
		assertEquals("[]/{}/devices/{}/child", paths.get(3));
		assertEquals("[]/{}/devices/{}/child", paths.get(4));
		assertEquals("{}/a/b/c/d/e/f", paths.get(5));

		assertValueCount(6, parents);
		assertEquals("{}", parents.first());
		assertEquals("{}", parents.get(1));
		assertEquals("{}", parents.get(2));
		assertEquals("child", parents.get(3));
		assertEquals("child", parents.get(4));
		assertEquals("f", parents.get(5));

		assertValueCount(6, parents2);
		assertEquals("{}", parents2.first());
		assertEquals("{}", parents2.get(1));
		assertEquals("{}", parents2.get(2));
		assertEquals("devices", parents2.get(3));
		assertEquals("devices", parents2.get(4));
		assertEquals("d", parents2.get(5));
	}

	@Test
	public void dataTypes () {
		JsonMatcher matcher = new JsonMatcher();
		Array<JsonValue> values = new Array();
		matcher
			.addPattern("*/devices/*/(maxCellTemp,temperature,dc_switch_off,admin_state_str,sleep_enabled,device_status,object)");
		matcher.setProcessor(value -> copy(value, values));
		matcher.parse(json);

		assertValueCount(1, values);
		JsonValue value = values.first();
		value.getLong("maxCellTemp");
		value.getDouble("temperature");
		assertTrue(value.has("dc_switch_off"));
		assertTrue(value.get("dc_switch_off").isNull());
		value.getString("admin_state_str");
		value.getBoolean("sleep_enabled");
		value.get("device_status").asStringArray();
		assertTrue(value.get("object").type() == ValueType.object);
	}

	@Test
	public void filtering () {
		{ // Reject by pattern index.
			JsonMatcher matcher = new JsonMatcher();
			Array<JsonValue> values = new Array();
			int enpower = matcher.addPattern("*/devices/*@/(serial_num)", value -> copy(value, values));
			int encharge = matcher.addPattern("*/devices/*@/(serial_num,percentFull)", value -> copy(value, values));
			matcher.addPattern("*/(type@)", value -> {
				if (value.equalsString("ENPOWER"))
					matcher.reject(encharge);
				else if (value.equalsString("ENCHARGE"))
					matcher.reject(enpower);
				else
					fail("Unexpected type: " + value);
			});
			matcher.parse(json);

			assertValueCount(3, values);
			assertEquals("{serial_num:32131444,percentFull:100}, {percentFull:75,serial_num:234234211}, 9834711", toString(values));
		}
		{ // Reject current pattern, storing the last encountered type across patterns.
			JsonMatcher matcher = new JsonMatcher();
			Array<JsonValue> values = new Array();
			String[] type = {""};
			matcher.addPattern("*/(type@)", value -> type[0] = value.asString());
			matcher.addPattern("*/devices/*@/(serial_num)", value -> {
				if (type[0].equals("ENPOWER"))
					copy(value, values);
				else
					matcher.reject();
			});
			matcher.addPattern("*/devices/*@/(serial_num,percentFull)", value -> {
				if (type[0].equals("ENCHARGE")) //
					copy(value, values);
				else
					matcher.reject();
			});
			matcher.parse(json);

			assertValueCount(3, values);
			assertEquals("{serial_num:32131444,percentFull:100}, {percentFull:75,serial_num:234234211}, 9834711", toString(values));
		}
		{ // Reject current pattern, checking type in the same pattern.
			JsonMatcher matcher = new JsonMatcher();
			Array<JsonValue> values = new Array();
			matcher.addPattern("*/(type@),devices/*@/(serial_num)", value -> {
				if (value.nameEquals("type")) {
					if (!value.equalsString("ENPOWER")) matcher.reject();
				} else
					copy(value, values);
			});
			matcher.addPattern("*/(type@),devices/*@/(serial_num,percentFull)", value -> {
				if (value.nameEquals("type")) {
					if (!value.equalsString("ENCHARGE")) matcher.reject();
				} else
					copy(value, values);
			});
			matcher.parse(json);

			assertValueCount(3, values);
			assertEquals("{serial_num:32131444,percentFull:100}, {percentFull:75,serial_num:234234211}, 9834711", toString(values));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern1 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("path/(to),nowhere,/"); // Empty match
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern2 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("(other),path()"); // No / before ()
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern3 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("(other),path(name"); // Unmatched (
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern4 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("path/name"); // No capture
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern5 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a//b/c/(value)"); // Double slash
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern6 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a//b/(c)"); // Double slash
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern7 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a/**,b/(c)"); // Match adjacent to **
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern8 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a/[]b/(c)"); // Misplaced []
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern9 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a/@b/(c)"); // Misplaced @
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern10 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a/@b/(c)"); // Misplaced @
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern11 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a/(b/c)"); // Invalid ()
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern12 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("/b/(c)"); // Start /
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern13 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a/,/(b)"); // Empty matches
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern14 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a/b[]/(c)"); // [] without capture
	}

	@Test(expected = IllegalStateException.class)
	public void invalidPattern15 () {
		new JsonMatcher("a/b/(c@)").parseValue("{}"); // parseValue disallows @
	}

	static void test (String json, String pattern, String... expected) {
		test(null, json, new String[] {pattern}, expected);
	}

	static void test (String json, String[] patterns, String... expected) {
		test(null, json, patterns, expected);
	}

	static void test (@Null String notParsedValue, String json, String[] patterns, String... expected) {
		Array<JsonValue> values = new Array();
		boolean[] ended = new boolean[1];
		JsonMatcher matcher = new JsonMatcher() {
			@Override
			protected void value (JsonToken name, JsonToken value) {
				if (notParsedValue != null && name.equals(notParsedValue))
					fail("Should have ended before parsing value: " + notParsedValue);
				super.value(name, value);
			}

			@Override
			public void end () {
				super.end();
				ended[0] = true;
			}
		};
		matcher.setProcessor(value -> copy(value, values));
		for (String pattern : patterns)
			matcher.addPattern(pattern);
		matcher.parse(json);
		try {
			assertValueCount(expected.length, values);
			for (int i = 0, n = expected.length; i < n; i++) {
				JsonValue value = values.get(i);
				assertEquals("Pattern " + i, expected[i], value.toJson(OutputType.minimal));
			}
			if (notParsedValue != null && !ended[0]) fail("Should have ended but did not");
		} catch (AssertionError ex) {
			printResults(matcher, values, json, patterns, expected);
			throw ex;
		}
	}

	static void printResults (JsonMatcher matcher, Array<JsonValue> values, String json, String[] patterns, String... expected) {
		System.out.println(" JSON: " + json);
		if (patterns.length == 1)
			System.out.println(" Pattern: " + patterns[0]);
		else
			System.out.println("Patterns: " + Arrays.toString(patterns));
		System.out.println("  Parsed: " + toString(matcher, patterns));
		System.out.println("Expected: " + expected.length + " "
			+ Arrays.toString(expected).replace("\n", "\\n").replace("\t", "\\t").replaceAll("^\\[|\\]$", ""));
		System.out.println("  Actual: " + values.size + " " + toString(values));
	}

	static void assertValueCount (int count, Array<JsonValue> values) {
		if (values.size != count) System.out.println("Actual: " + values.size + " " + toString(values));
		assertEquals("Wrong match count", count, values.size);
	}

	static String toString (JsonMatcher matcher, String[] patterns) {
		CharArray buffer = new CharArray();
		for (String pattern : patterns) {
			if (pattern.isEmpty())
				buffer.append("\"\"", ", ");
			else
				buffer.append(PatternParser.parse(matcher, pattern, null).toString(), ", ");
		}
		return buffer.replaceAll("\n", "\\n").replaceAll("\t", "\\t").toString();
	}

	static String toString (Array<JsonValue> values) {
		CharArray buffer = new CharArray();
		for (JsonValue value : values)
			buffer.append(value.toJson(OutputType.minimal), ", ");
		return buffer.replaceAll("\n", "\\n").replaceAll("\t", "\\t").toString();
	}

	static void copy (JsonValue value, Array<JsonValue> values) {
		values.add(new JsonValue(value));
	}

	static private final String json = // @off
		"[{\n"
		+ "type: ENCHARGE,\n"
		+ "devices: [\n"
		+ "	{\n"
		+ "		part_num: 830-00703-r84,\n"
		+ "		serial_num: \"32131444\",\n"
		+ "		installed: 17519017,\n"
		+ "		device_status: [\n"
		+ "			envoy.global.ok,\n"
		+ "			prop.done\n"
		+ "		],\n"
		+ "		last_rpt_date: 1753239176,\n"
		+ "		admin_state: 6,\n"
		+ "		admin_state_str: ENCHG_STATE_READY,\n"
		+ "		created_date: 1751974017,\n"
		+ "		img_load_date: 1751974017,\n"
		+ "		img_pnum_running: \"2.0.8116_rel/22.33\",\n"
		+ "		bmu_fw_version: 2.1.38,\n"
		+ "		communicating: true,\n"
		+ "		sleep_enabled: false,\n"
		+ "		percentFull: 100,\n"
		+ "		temperature: 31.4,\n"
		+ "		maxCellTemp: 31,\n"
		+ "		reported_enc_grid_state: grid-tied,\n"
		+ "		comm_level_sub_ghz: 5,\n"
		+ "		comm_level_2_4_ghz: 5,\n"
		+ "		led_status: 14,\n"
		+ "		dc_switch_off: null,\n"
		+ "		child: { value: 1 },\n"
		+ "		encharge_rev: 1,\n"
		+ "		encharge_capacity: 3360,\n"
		+ "		phase: ph-a,\n"
		+ "		der_index: 1,\n"
		+ "		object: {}\n"
		+ "	},\n"
		+ "	{\n"
		+ "		part_num: 830-00703-r84,\n"
		+ "		installed: 17518704,\n"
		+ "		percentFull: 75,\n"
		+ "		serial_num: \"234234211\",\n"
		+ "		child: { value: 2 },\n"
		+ "		device_status: [\n"
		+ "			envoy.global.failure,\n"
		+ "			prop.waiting\n"
		+ "		]\n"
		+ "	}\n"
		+ "]},{\n"
		+ "type: ENPOWER,\n"
		+ "devices: [{\n"
		+ "	serial_num: \"9834711\",\n"
		+ "}]\n"
		+ "}]"; // @on

	@Rule public TestWatcher watcher = new TestWatcher() {
		protected void failed (Throwable cause, Description desc) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			cause.printStackTrace(pw);
			String trimmed = sw.toString() //
				.replace("\t", "   ") //
				.replaceAll("^[^:]*\\.([^:]+?): ", "[$1] ") //
				.lines().filter(line -> {
					String stripped = line.stripLeading();
					return stripped.isEmpty() || !stripped.startsWith("at ") || stripped.startsWith("at com.badlogic.gdx");
				}).collect(Collectors.joining("\n"));
			System.out.println("--- " + desc.getTestClass().getSimpleName() + ": " + desc.getMethodName() + " ---");
			System.out.println(trimmed);
			System.out.println();
		}
	};
}


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

public class JsonMatcherTests {
	@Test
	public void singlePatterns () {
		test( // *
			json, //
			"*/(type)", //
			"{type=ENCHARGE}"); // First.

		test( // @
			json, //
			"*@/(type)", //
			"{type=ENCHARGE}", "{type=ENPOWER}");

		test( // * for nested paths
			json, //
			"*/devices/*/(serial_num,percentFull)", //
			"{percentFull=100, serial_num=32131444}");

		test( // Multiple wildcards in sequence
			"[{a:[{deep:value}]}]", //
			"*/*/*/(deep)", //
			"{deep=value}");

		test( // Pattern ending with wildcard (not useful)
			"{root:{a:1,b:2}}", //
			"root/(a)/*", //
			"{a=1}");

		test( // ** recursive wildcard
			json, //
			"*/(type)", //
			"{type=ENCHARGE}");

		test( // ** to find value at any depth
			json, //
			"*/devices/**/(value)", //
			"{value=1}");

		test( // ** recursive wildcard
			"{a:{b:{value:1}},c:{value:2},value:3}", //
			"**/(value)", //
			"{value=1}");

		test( // ** followed by literal
			"{servers:{prod:{config:{port:8080}}},config:{port:9090}}", //
			"**/config/(port)", //
			"{port=8080}");

		test( // @ process each array element
			json, //
			"*/devices/*@/(serial_num,percentFull)", //
			"{percentFull=100, serial_num=32131444}", "{percentFull=75, serial_num=234234211}", "{serial_num=9834711}");

		test( // @ with no array
			"{data:{value:not-an-array}}", //
			"data/(value)", //
			"{value=not-an-array}");

		test( // @ with nested @
			"{outer:[{id:1,inner:[{name:A},{name:B}]}]}", //
			"outer/*@/(id),inner@/*/(name[])", //
			"{id=1}", "{name=[A, B]}");

		test( // @ followed by * with multiple matches
			"{data:[{items:[{value:1},{value:2}]}]}", //
			"data/*@/items/*/(value)", //
			"{value=2}");

		test( // * followed by @
			"{data:[{items:[{value:1},{value:2}]}]}", //
			"data/*/items/*@/(value)", //
			"{value=1}", "{value=2}");

		test( // **@ recursive process
			json, //
			"*/devices/**@/(value)", //
			"{value=1}", "{value=2}");

		test( // **@ recursive process
			"{id:1,child:{id:2,nested:{id:3}}}", //
			"**@/(id)", //
			"{id=1}", "{id=2}", "{id=3}");

		test( // Literal path after nested object
			"{config:{version:1.0,debug:{level:info},name:MyApp}}", //
			"config/(name)", //
			"{name=MyApp}");

		test( // Multiple literal paths at same level
			"{servers:{prod:{host:prod.example.com},dev:{host:dev.example.com}}}", //
			"servers/prod/(host)", //
			"{host=prod.example.com}");

		test( // Wildcard collecting from siblings
			"{data:{item1:{nested:{deep:true},id:first},item2:{id:second}}}", //
			"data/*/(id)", //
			"{id=first}");

		test( // Empty JSON
			"{}", //
			"test/(value)");

		test( // Empty array
			"[]", //
			"*/(name)");

		test( // Deeply nested structure
			"{a:{b:{c:{d:{e:{f:{value:deep}}}}}}}", //
			"a/b/c/d/e/f/(value)", //
			"{value=deep}");

		test( // Array of arrays
			"{data:[[{id:1},{id:2}],[{id:3}]]}", //
			"data/*/*/(id)", //
			"{id=1}");

		test( // Object with no fields, process each
			"{empty:[{},{}]}", //
			"empty/(*@)");

		test( // Object with no fields, as array
			"{empty:[{},{}]}", //
			"empty/(*)");

		test( // Literal path no match
			"{users:{johnny:{age:30}}}", //
			"users/john/(hair)");

		test( // Literal path no match
			"{users:{johnny:{age:30}}}", //
			"users/john/*/(age)");

		test( // Test dead node, no match
			"{data:{item1:{nested:{deep:true},id:first},item2:{id:second}}}", //
			"data/(deep)");

		test( // Pattern that matches nothing
			"{other:{path:{value:test}}}", //
			"nonexistent/path/(value)");

		test( // Same field name at different levels
			"{outer:{name:A,inner:{name:B}}}", //
			"outer/(name),inner/(name)", //
			"{name=B}");

		test( // Field names with special characters
			"{data:{field.with.dots:1,field-with-dashes:2,field_with_underscores:3}}", //
			"data/(field.with.dots,field-with-dashes,field_with_underscores)", //
			"{field-with-dashes=2, field.with.dots=1, field_with_underscores=3}");

		test( // Collect array items
			"{data:{z:1,x:{ok:true},items:[a,b,c]}}", //
			"data/items/(*@)", //
			"{=a}", "{=b}", "{=c}");

		test( // Collect array itself
			"{data:{z:1,x:{ok:true},items:[a,b,c]}}", //
			"data/(items)", //
			"{items=[a, b, c]}");

		test( // Collect array itself with []
			"{data:{items:[a,b,c]}}", //
			"data/(items)", //
			"{items=[a, b, c]}");

		test( // Collect empty array
			"{data:{empty:[]}}", //
			"data/(empty)", //
			"{empty=[]}");

		test( // @ with array collection
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"items/(*@)", //
			"{tags=[red, big]}", "{tags=[blue]}");

		test( // @ with array collection by name
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"items/*@/(tags)", //
			"{tags=[red, big]}", "{tags=[blue]}");

		test( // * for whole array
			json, //
			"*/devices/*/(device_status)", //
			"{device_status=[envoy.global.ok, prop.done]}"); // 1 match because processing stops.

		test( // @ for whole arrays
			json, //
			"*/devices/*@/(device_status)", //
			"{device_status=[envoy.global.ok, prop.done]}", "{device_status=[envoy.global.failure, prop.waiting]}");

		test( // (*) for single array values
			json, //
			"*/devices/*/device_status/(*)", //
			"{=envoy.global.ok}"); // Only first for single match.

		test( // (*) for array values
			json, //
			"*/devices/*/device_status/(*[])", //
			"{=[envoy.global.ok, prop.done, envoy.global.failure, prop.waiting]}");

		test( // [@] for array values
			json, //
			"*/devices/*/device_status/(*@)", //
			"{=envoy.global.ok}", "{=prop.done}", "{=envoy.global.failure}", "{=prop.waiting}");

		test( // * with array collection
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"items/*/(tags[])", //
			"{tags=[[red, big], [blue]]}");

		test( // **@ with object collection
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"**@/(tags)", //
			"{tags=[red, big]}", "{tags=[blue]}");

		test( // ** before [@] for per array item processing
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"**/tags/(*@)", //
			"{=red}", "{=big}", "{=blue}");

		test( // skip items and process each
			"{items:[{tags:[red,big]},{tags:[blue]}]}", //
			"*/*/(*@)", //
			"{tags=[red, big]}", "{tags=[blue]}");

		test( // process each at root array
			"[{tags:[red,big]},{tags:[blue]}]", //
			"(*@)", //
			"{tags=[red, big]}", "{tags=[blue]}");

		test( // process each at root object
			"{tags1:[red,big],tags2:[blue]}", //
			"(*@)", //
			"{tags1=[red, big]}", "{tags2=[blue]}");

		test( // process each at root object
			"{items1:{tags:[red,big]},items2:{tags:[blue]}}", //
			"(*@)", //
			"{items1={tags=[red, big]}}", "{items2={tags=[blue]}}");

		test( // Multiple array collections
			"{data:{a:[1,2],b:[3,4],c:[5,6]}}", //
			"data/(a,b,c)", //
			"{a=[1, 2], b=[3, 4], c=[5, 6]}");

		test( // Mix regular and array collection
			"{a:{x:1,y:2},b:{x:3,y:4}}", //
			"*/(x,y[])", //
			"{x=3, y=[2, 4]}");

		test( // **@ with array collection
			"{a:{val:1},b:{c:{val:2},d:{val:3}}}", //
			"**@/(val[])", //
			"{val=[1]}", "{val=[2]}", "{val=[3]}");

		test( // Deep recursive wildcard
			"{a:{a:{a:{a:{a:{x:deep}}}}}}", //
			"**/(x)", //
			"{x=deep}");

		test( // @ followed by **@
			"{items:[{deep:{a:{x:1},b:{x:2}}}]}", //
			"items/*@/deep/**@/(x)", //
			"{x=1}", "{x=2}");

		test( // ** at root level
			"{x:1,a:{x:2},b:{c:{x:3}}}", //
			"**/(x[])", //
			"{x=[1, 2, 3]}");

		test( // @ on primitive array
			"{tags:[red,blue,green]}", //
			"tags/(*@)", //
			"{=red}", "{=blue}", "{=green}");

		test( // Nested arrays with [@]
			"{matrix:[[1,2],[3,4]]}", //
			"matrix/*/(*@)", //
			"{=1}", "{=2}", "{=3}", "{=4}");

		test( // @ with multiple fields
			"[{id:1,name:A},{id:2,name:B}]", //
			"*@/(id,name)", //
			"{id=1, name=A}", "{id=2, name=B}");

		test( // ** followed by literal
			"{a:{b:{target:{x:1}},c:{target:{x:2}}}}", //
			"**/target/(x[])", //
			"{x=[1, 2]}");

		test( // [@] for nested object values
			"{data:{a:{val:1},b:{val:2},c:{val:3}}}", //
			"data/*/(*@)", //
			"{val=1}", "{val=2}", "{val=3}");

		test( // Escaped quotes
			"{data:'He said \\\"hello\\\"'}", //
			"(data)", //
			"{data='He said \"hello\"'}");

		test( // Multiple @ wildcards
			"{list:[{items:[a,b]},{items:[c,d]}]}", //
			"list/*@/items/(*@)", //
			"{=a}", "{=b}", "{=c}", "{=d}");

		test( // * and ** combination
			"{a:{b:{c:{d:1}}},x:{b:{c:{d:2}}}}", //
			"*/b/**/(d)", //
			"{d=1}");

		test( // * and ** combination, match first
			"{a:{b:{c:{d:1}}},x:{b:{c:{d:2}}}}", //
			"*/b/**/(c)", //
			"{c={d=1}}");

		test( // * and ** combination, as array
			"{a:{b:{c:{d:1}}},x:{b:{c:{d:2}}}}", //
			"*/b/**/(c[])", //
			"{c=[{d=1}, {d=2}]}");

		test( // Null value
			"{data:{name:null,value:123}}", //
			"data/(name,value)", //
			"{name=null, value=123}");

		test( // Null in array
			"{items:[null,a,null,b]}", //
			"items/(*@)", //
			"{=null}", "{=a}", "{=null}", "{=b}");

		test( // Null with array collection
			"{data:[{x:null},{x:1},{x:null}]}", //
			"data/*/(x[])", //
			"{x=[null, 1, null]}");

		test( // Boolean and numeric values
			"{config:{debug:true,port:8080,ratio:3.14}}", //
			"config/(debug,port,ratio)", //
			"{debug=true, port=8080, ratio=3.14}");

		test( // Empty field name
			"{\"\":empty,normal:value}", //
			"('')", //
			"{=empty}");

		test( // Empty field name
			"{\"\":empty,normal:value}", //
			"('',normal)", //
			"{=empty, normal=value}");

		test( // Unicode in field names
			"{user:{名前:太郎,età:25,émoji:🚀}}", //
			"user/(名前,età,émoji)", //
			"{età=25, émoji=🚀, 名前=太郎}");

		test( // Very deeply nested (10 levels)
			"{a:{b:{c:{d:{e:{f:{g:{h:{i:{j:{value:deep}}}}}}}}}}}", //
			"a/b/c/d/e/f/g/h/i/j/(value)", //
			"{value=deep}");

		test( // ** matching at multiple depths
			"{a:{x:1,b:{x:2,c:{x:3}}}}", //
			"**/(x)", //
			"{x=1}");

		test( // Multiple ** in pattern
			"{a:{b:{c:{d:{e:value}}}}}", //
			"**/b/**/d/(e)", //
			"{e=value}");

		test( // @ at multiple levels simultaneously
			"{data:[{items:[1,2]},{items:[3,4]}]}", //
			"data/*@/items/(*@)", //
			"{=1}", "{=2}", "{=3}", "{=4}");

		test( // Mixed object and array wildcards
			"{data:[{a:1},{b:2}],meta:{items:[{c:3},{d:4}]}}", //
			"*/*/*@/(*)", //
			"{c=3}", "{d=4}");

		test( // Mixed object and array wildcards, as arrays
			"{data:[{a:1},{b:2}],meta:{items:[{c:3},{d:4}]}}", //
			"*/*/*@/(*[])", //
			"{c=[3]}", "{d=[4]}");

		test( // Numeric-like field names
			"{\"123\":{\"456\":value,normal:other}}", //
			"123/(456,normal)", //
			"{456=value, normal=other}");

		test( // Whitespace in pattern
			"{\"  field \n \":{\" \t nested \t\":value}}", //
			"  field \n /( \t nested \t)", //
			"{ \t nested \t=value}");

		test( // **@ matching objects at all levels
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**@/(*)", //
			"{a=1}", "{b={d={e=3}, c=2}}");

		test( // Complex array of arrays of objects
			"{data:[[[{x:1}]],[[{x:2}],[{x:3}]]]}", //
			"data/*/*/*/(x)", //
			"{x=1}");

		test( // @ on empty array
			"{empty:[]}", //
			"empty/(*@)");

		test( // (*) on empty object
			"{empty:{}}", //
			"empty/(*)");

		test( // Multiple patterns matching same field
			"{user:{profile:{name:John,age:30}}}", //
			"user/profile/(name)", //
			"{name=John}");

		test( // **@ with nested matches
			"{a:{val:1,b:{val:2,c:{val:3}}}}", //
			"a/**@/(val)", //
			"{val=1}", "{val=2}", "{val=3}");

		test( // Very large array
			"{items:[" + "1,".repeat(99) + "100]}", //
			"items/(*[])", //
			"{=[" + "1, ".repeat(99) + "100]}");

		test( // @ followed by literal path
			"{list:[{user:{name:A}},{user:{name:B}}]}", //
			"list/*@/user/(name)", //
			"{name=A}", "{name=B}");

		test( // ** followed by @
			"{deep:{nested:{items:[a,b,c]}}}", //
			"**/items/(*@)", //
			"{=a}", "{=b}", "{=c}");

		test( // Collecting same field at different depths
			"{name:root,child:{name:nested}}", //
			"(name),child/(name)", //
			"{name=nested}");

		test( // ** with array collection across levels
			"{a:{x:[1,2]},b:{c:{x:[3,4]}}}", //
			"**/(x[])", //
			"{x=[[1, 2], [3, 4]]}");

		test( // Empty object at root
			"{}", //
			"(*)");

		test( // @ on object (not array)
			"{users:{john:{age:30},jane:{age:25}}}", //
			"users/*@/(age)", //
			"{age=30}", "{age=25}");

		test( // Multiple @ in different branches
			"{left:[{x:1},{x:2}],right:[{x:3},{x:4}]}", //
			"*/*@/(x)", //
			"{x=1}", "{x=2}", "{x=3}", "{x=4}");

		test( // Pattern matching primitive at root
			"42", //
			"(value)");

		test( // String at root level
			"\"hello\"", //
			"(*)", //
			"{=hello}");

		test( // **@ starting from non-root
			"{skip:{this:{find:{me:{x:1}}}}}", //
			"skip/**/find/**@/(x)", //
			"{x=1}");

		test( // Collecting fields with special JSON characters
			"{\"field\\nwith\\nnewlines\":1,\"field\\twith\\ttabs\":2}", //
			"(field\nwith\nnewlines,field\twith\ttabs)", //
			"{field\twith\ttabs=2, field\nwith\nnewlines=1}");

		test( // @ with mixed types in array
			"{mixed:[1,string,true,null,{obj:value}]}", //
			"mixed/(*@)", //
			"{=1}", "{=string}", "{=true}", "{=null}", "{obj=value}");

		test( // Overlapping **@ and ** patterns
			"{a:{b:{c:{x:1,d:{x:2}}}}}", //
			"**@/b/**/(x),d/(x)", //
			"{x=1}", "{x=2}");

		test( // Overlapping **@ patterns
			"{a:{b:{c:{x:1,d:{x:2}}}}}", //
			"**@/b/**@/(x),d/(x)", //
			"{x=1}", "{x=2}");

		test( // Overlapping **@ and ** patterns, test pop with multiple branches
			"[{a:{b:{x:1,bb:{c:{x:2}}}}},{a:{b:{x:3,bb:{c:{x:4}}}}}]", //
			"**@/b/(x),*/**/c/(x)", //
			"{x=1}", "{x=2}", "{x=3}", "{x=4}");

		test( // Overlapping ** and **@ patterns
			"{a:{b:{x:1,bb:{c:{x:2,d:{x:3}}}}}}", //
			"**/b/(x),bb/**@/c/(x),d/(x)", //
			"{x=1}", "{x=2}", "{x=3}");

		test( // (*) with nested objects
			"{data:{a:{deep:1},b:{deep:2}}}", //
			"(*)", //
			"{data={a={deep=1}, b={deep=2}}}");

		test( // (*) with nested objects
			"{data:{a:{deep:1},b:{deep:2}}}", //
			"data/(*)", //
			"{a={deep=1}}"); // First

		test( // (*@) with nested objects
			"{data:{a:{deep:1},b:{deep:2}}}", //
			"data/(*@)", //
			"{a={deep=1}}", "{b={deep=2}}");

		test( // @ collecting entire objects
			"{list:[{id:1,data:{x:10}},{id:2,data:{x:20}}]}", //
			"list/*@/(data)", //
			"{data={x=10}}", "{data={x=20}}");

		test( // Pattern with only wildcards
			"{a:{b:{c:value}}}", //
			"*/*/(c)", //
			"{c=value}");

		test( // **@ at root collecting arrays
			"{items:[1,2,3]}", //
			"**@/(items)", //
			"{items=[1, 2, 3]}");

		test( // Complex nested with multiple array depths
			"{root:[{level1:[{level2:[{value:deep}]}]}]}", //
			"root/*@/level1/*@/level2/*@/(value)", //
			"{value=deep}");

		test( // @ on array of primitives and objects mixed
			"{items:[1,{x:2},3,{x:4}]}", //
			"items/*@/(x)", //
			"{x=2}", "{x=4}");

		test( // Collecting nothing from existing structure
			"{a:{b:{c:1}},d:{e:{f:2}}}", //
			"x/y/(z)");

		test( // ** with literal after multiple levels
			"{a:{b:{target:miss},c:{d:{target:{hit:true}}}}}", //
			"**/d/target/(hit)", //
			"{hit=true}");

		test( // Array index-like field names
			"{\"0\":first,\"1\":second,\"2\":third}", //
			"(0,1,2)", //
			"{0=first, 1=second, 2=third}");

		test( // @ with empty field collection
			"{items:[{},{},{}]}", //
			"items/*@/(*)");

		test( // Very long field name
			"{\"" + "x".repeat(100) + "\":value}", //
			"(" + "x".repeat(100) + ")", //
			"{" + "x".repeat(100) + "=value}");

		test( // **@ with @ combination
			"{level1:[{level2:[{x:1},{x:2}]}]}", //
			"**@/level2/*@/(x)", //
			"{x=1}", "{x=2}");

		test( // Object with array-like structure
			"{data:{\"length\":3,\"0\":a,\"1\":b,\"2\":c}}", //
			"data/(length,0,1,2)", //
			"{0=a, 1=b, 2=c, length=3}");

		test( // Escaped characters in values
			"{data:{value:\"line1\\nline2\\ttab\"}}", //
			"data/(value)", //
			"{value=line1\nline2\ttab}");

		test( // @ at root with nested collection
			"[{a:{b:[1,2]}},{a:{b:[3,4]}}]", //
			"*@/a/b/(*)", //
			"{=2}", "{=4}"); // Only last.

		test( // @ at root with nested collection
			"[{a:{b:[1,2]}},{a:{b:[3,4]}}]", //
			"*@/a/b/(*[])", //
			"{=[1, 2]}", "{=[3, 4]}");

		test( // Multiple array collections with overlap
			"{x:[1,2],y:[2,3],z:[3,4]}", //
			"(x,y,z)", //
			"{x=[1, 2], y=[2, 3], z=[3, 4]}");

		test( // ** collecting all with *
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**/(*)", //
			"{a=1}"); // First

		test( // ** collecting all with *@
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**/(*@)", //
			"{a=1}", "{b={d={e=3}, c=2}}");

		test( // **@ collecting all with *
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**@/(*)", //
			"{a=1}", "{b={d={e=3}, c=2}}");

		test( // **@ collecting all with *@
			"{a:1,b:{c:2,d:{e:3}}}", //
			"**@/(*@)", //
			"{a=1}", "{b={d={e=3}, c=2}}");

		test( // @ on non-array value
			"{item:not-an-array}", //
			"item/(*@)");

		test( // Circular reference simulation (same field names at different levels)
			"{node:{value:1,node:{value:2,node:{value:3}}}}", //
			"**/node/(value)", //
			"{value=1}");

		test( // Multiple fields with same prefix
			"{user:1,userName:2,userAge:3}", //
			"(user,userName,userAge)", //
			"{user=1, userAge=3, userName=2}");

		test( // Pattern matching boolean false
			"{flags:{active:false,debug:false,enabled:true}}", //
			"flags/(active,debug,enabled)", //
			"{active=false, debug=false, enabled=true}");

		test( // Multiple patterns capturing same field name
			"{user:{name:John},profile:{name:Jane}}", //
			"user/(name)", // This should stop after first match
			"{name=John}");

		test( // Field name appears at multiple depths
			"{x:1,nested:{x:2,deep:{x:3}}}", //
			"(x),nested/(x)", //
			"{x=2}");

		test( // Same field captured through different paths
			"{data:{id:1,user:{id:2}}}", //
			"data/(id),user/(id)", //
			"{id=2}");

		test( // Pattern that could match same field twice
			"{a:{x:1},b:{x:2}}", //
			"*/(x)", //
			"{x=1}"); // First.

		test( // Overwriting with non-array collection
			"{items:[{x:1},{x:2},{x:3}]}", //
			"items/*/(x)", //
			"{x=1}"); // First.

		test( // Complex overwrite scenario
			"{level1:{name:A,level2:{name:B}},other:{name:C}}", //
			"*/(name)", //
			"{name=A}"); // First.

		test( // Capture 2 values with same field name with array collection
			"{first:{x:1,second:{x:2}}}", //
			"first/(x[]),second/(x[])", //
			"{x=[1, 2]}");

		test( // Multiple wildcards matching same structure
			"{data:{a:{id:1},b:{id:2}}}", //
			"data/*/(id)", //
			"{id=1}"); // First.

		test( // Multiple wildcards matching same structure with array collection
			"{data:{a:{id:1},b:{id:2}}}", //
			"data/*/(id[])", //
			"{id=[1, 2]}"); // First.

		test( // Same field many levels with array collection
			"{items:[{x:1},{x:2},{x:3}]}", //
			"items/*/(x[])", //
			"{x=[1, 2, 3]}");

		test( // Multiple values where some repeat
			"{data:{x:1,y:2}}", //
			"data/(x,y,x)", //
			"{x=1, y=2}");

		test( // Many **
			"{data:{x:1,y:2}}", //
			"**/**/**/data/**/**/**/(x,y,x)", //
			"{x=1, y=2}");

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
			"{port=8080}");
	}

	@Test
	public void unescaping () {
		test( // Escaped quotes
			"{data:\"He said \\\"hello\\\"\"}", //
			"(data)", //
			"{data=He said \"hello\"}");

		test( // Escaped backslash
			"{path:\"C:\\\\Users\\\\file.txt\"}", //
			"(path)", //
			"{path=C:\\Users\\file.txt}");

		test( // Escaped newline
			"{text:\"Line 1\\nLine 2\\nLine 3\"}", //
			"(text)", //
			"{text=Line 1\nLine 2\nLine 3}");

		test( // Escaped tab
			"{data:\"Column1\\tColumn2\\tColumn3\"}", //
			"(data)", //
			"{data=Column1\tColumn2\tColumn3}");

		test( // Escaped carriage return
			"{text:\"Windows\\r\\nLine ending\"}", //
			"(text)", //
			"{text=Windows\r\nLine ending}");

		test( // Escaped forward slash
			"{url:\"https:\\/\\/example.com\\/path\"}", //
			"(url)", //
			"{url=https://example.com/path}");

		test( // Escaped backspace
			"{data:\"Before\\bAfter\"}", //
			"(data)", //
			"{data=Before\bAfter}");

		test( // Escaped form feed
			"{data:\"Page1\\fPage2\"}", //
			"(data)", //
			"{data=Page1\fPage2}");

		test( // Unicode escape sequences
			"{emoji:\"\\u2764\\uFE0F\"}", // Heart emoji
			"(emoji)", //
			"{emoji=❤️}");

		test( // Unicode escape for special chars
			"{text:\"\\u00A9 2024 Company\"}", // Copyright symbol
			"(text)", //
			"{text=© 2024 Company}");

		test( // Multiple escapes in one string
			"{complex:\"Line 1\\n\\tIndented\\n\\\"Quoted\\\"\\nC:\\\\path\"}", //
			"(complex)", //
			"{complex=Line 1\n\tIndented\n\"Quoted\"\nC:\\path}");

		test( // Escaped field names
			"{\"field\\nwith\\nnewlines\":\"value1\",\"field\\twith\\ttab\":\"value2\"}", //
			"(field\nwith\nnewlines,field\twith\ttab)", //
			"{field\twith\ttab=value2, field\nwith\nnewlines=value1}");

		test( // Mix of unicode and regular escapes
			"{mixed:\"\\u0048ello\\nWorld\\t\\u0021\"}", // H and !
			"(mixed)", //
			"{mixed=Hello\nWorld\t!}");

		test( // Empty string with escapes
			"{empty:\"\",\"escaped\":\"\\n\\t\"}", //
			"(empty,escaped)", //
			"{empty=, escaped=\n\t}");

		test( // Nested objects with escaped values
			"{user:{\"name\":\"John \\\"Johnny\\\" Doe\",\"bio\":\"Line 1\\nLine 2\"}}", //
			"user/(name,bio)", //
			"{bio=Line 1\nLine 2, name=John \"Johnny\" Doe}");

		test( // Array with escaped values
			"{items:[\"\\\"quoted\\\"\",\"\\ttabbed\",\"new\\nline\"]}", //
			"items/(*@)", //
			"{=\"quoted\"}", "{=\ttabbed}", "{=new\nline}");

		test( // Unicode surrogate pairs
			"{emoji:\"\\uD83D\\uDE00\"}", // 😀
			"(emoji)", //
			"{emoji=😀}");

		test( // All escape sequences
			"{all:\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t\"}", //
			"(all)", //
			"{all=\" \\ / \b \f \n \r \t}");

		test( // Escaped at different levels
			"{level1:{escaped:\"\\\"value\\\"\"},level2:[{item:\"\\nitem\\n\"}]}", //
			"**/(escaped)", //
			"{escaped=\"value\"}");

		test( // Multiple unicode in sequence
			"{text:\"\\u0041\\u0042\\u0043\"}", // ABC
			"(text)", //
			"{text=ABC}");

		test( // Multiple unicode in sequence, unquoted
			"{text:\\u0041\\u0042\\u0043}", // ABC
			"(text)", //
			"{text=ABC}");

		test( // Unicode with array processing
			"{names:[\"\\u4E2D\\u6587\",\"\\u65E5\\u672C\\u8A9E\"]}", // Chinese, Japanese
			"names/(*@)", //
			"{=中文}", "{=日本語}");

		test( // Very long escaped string
			"{long:\"" + "\\n".repeat(10) + "\"}", //
			"(long)", //
			"{long=" + "\n".repeat(10) + "}");

		test( // Escaped within wildcards
			"{data:{\"field\\nname\":\"value\\there\"}}", //
			"data/(*)", //
			"{field\nname=value\there}");

		test( // @ with escaped array values
			"{list:[{text:\"\\\"A\\\"\",value:1},{text:\"\\\"B\\\"\",value:2}]}", //
			"list/*@/(text,value)", //
			"{text=\"A\", value=1}", "{text=\"B\", value=2}");

		test( // ** then *
			json, //
			"**/*/(serial_num)", //
			"{serial_num=32131444}");

		test( // @ with objects containing @
			"{items:[{\"@\":special},{normal:value}]}", //
			"items/*@/('@',normal)", //
			"{@=special}", "{normal=value}");

		test( // Escaping inside ''
			"{da\\\\ta:{it'ems:[a,b,c]}}", //
			"'da\\ta'/('it''ems')", //
			"{it'ems=[a, b, c]}");

		test( // Special characters inside ''
			"{*/()[\\\\]@',\\\\\\\\:{items:[a,b,c]}}", //
			"'*/()[\\]@'',\\\\'/(items)", //
			"{items=[a, b, c]}");
	}

	@Test
	public void multiplePatterns () {
		test( // Multiple patterns
			"{user:{name:John,age:30},meta:{version:1.0}}", new String[] { //
				"user/(name)", //
				"meta/(version)"},
			"{name=John}", "{version=1.0}");

		test( // Same field name, different patterns
			"{user:{name:John},profile:{user:{name:Jane}}}", new String[] { //
				"user/(name)", //
				"profile/user/(name)"},
			"{name=John}", "{name=Jane}");

		test( // Same field name, same patterns
			"{user:{name:John},profile:{user:{name:Jane}}}", new String[] { //
				"user/(name)", //
				"user/(name)", //
				"profile/user/(name)", //
				"profile/user/(name)"},
			"{name=John}", "{name=John}", "{name=Jane}", "{name=Jane}");

		test( // Overlapping matches
			"{a:{b:{c:1}}}", new String[] { //
				"**@/(b)", //
				"**@/(c)"},
			"{c=1}", "{b={c=1}}");
	}

	@Test
	public void earlyStop () {
		test("extra", // Early stop
			"{first:{id:1},second:{data:ignored},extra:should-not-parse}", new String[] { //
				"first/(id@)", //
				"(second)"},
			"{id=1}", "{second={data=ignored}}");

		test("extra", // Same name twice
			"{first:{id:1},second:{id:ignored},extra:should-not-parse}", new String[] { //
				"*/(id@)", //
				"*/(id)"},
			"{id=1}", "{id=1}");

		test("extra", // Multiple values
			"{first:{id:1},second:{id:ignored},extra:should-not-parse}", new String[] { //
				"(first,second)@"},
			"{first={id=1}}", "{second={id=ignored}}");

		test("extra", //
			"{first:{id:1},second:{id:ignored},extra:should-not-parse}", new String[] { //
				"second/(id@)"},
			"{id=ignored}");

		test("extra", // Multiple values
			"{first:{id:1},second:{id:ignored},third:{other:1},extra:should-not-parse}", new String[] { //
				"(first,second)@", //
				"*/(other)"},
			"{first={id=1}}", "{second={id=ignored}}", "{other=1}");

		test("extra", // Same field at different levels
			"{value:1,nested:{value:2},extra:{value:3}}", new String[] { //
				"(value@),*/(value@)"},
			"{value=1}", "{value=2}");
	}

	@Test
	public void rejection () {
		{ // Reject all except 1.
			Array<ObjectMap> maps = new Array();
			JsonMatcher matcher = new JsonMatcher();
			matcher.addPattern("*/(type@)", map -> {
				if (map.get("type").equals("ENCHARGE")) matcher.rejectAll();
			});
			matcher.addPattern("*/devices/*@/(serial_num,percentFull)", map -> copy(map, maps));
			matcher.parse(json);

			assertMapCount(1, maps);
			ObjectMap map = maps.first();
			assertEquals("9834711", map.get("serial_num"));
			assertNull(map.get("percentFull"));
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
			"{value=found}");

		rejectAll( // Reject at depth 2, but continue matching at depth 1
			"{items:[{bad:{reject:true}},{good:{value:1}}]}", //
			"items/*@/bad/(reject)", // Reject when finding bad
			"items/*@/good/(value)", // Should still find good in next item
			"{value=1}");

		rejectAll( // Rejection mid-array processing
			"{items:[{id:1},{id:2,skip:true},{id:3}]}", //
			"items/*/(skip@)", //
			"items/*@/(id,skip)", //
			"{id=1}", "{id=3}" //
		);

		rejectAll( // Rejection with nested ** patterns:
			"{a:{b:{reject:here,c:{d:{value:no}}},e:{c:{d:{value:yes}}}}}", //
			"**/b/(reject@)", //
			"**/c/**/d/(value)", // Should only find in e branch
			"{value=yes}");

		rejectAll( // One pattern rejects, other continues
			"{data:{type:[bad],info:important}}", //
			"data/type@/(*)", //
			"data@/(info)", // Should still capture
			"{info=important}");

		rejectAll( // Reject after collecting some values.
			"{a:{b:{target:{x:1}},c:{target:{x:2,reject:true}}},d:{target:{x:3}}}", //
			"**/(reject@)", //
			"**/(x[])", //
			"{x=[3]}"); // rejectAll removes first 2 matches.
	}

	static void rejectAll (String json, String rejectPattern, String pattern, String... expected) {
		Array<ObjectMap> maps = new Array();
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern(rejectPattern, map -> {
			matcher.rejectAll();
			matcher.clearAll();
		});
		matcher.addPattern(pattern, map -> copy(map, maps));
		matcher.parse(json);

		try {
			assertMapCount(expected.length, maps);
			for (int i = 0, n = expected.length; i < n; i++)
				assertEquals("Pattern " + i, expected[i], maps.get(i).toString());
		} catch (AssertionError ex) {
			printResults(maps, json, new String[] {rejectPattern, pattern}, expected);
			throw ex;
		}
	}

	@Test
	public void explicitStop () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("*/(type@)", map -> {
			if (map.get("type").equals("ENCHARGE")) matcher.stop(); // Stop parsing before any matches.
		});
		Array<ObjectMap> maps = new Array();
		matcher.addPattern("*/devices/*@/(serial_num,percentFull)", map -> copy(map, maps));
		matcher.parse(json);

		assertMapCount(0, maps);
	}

	@Test
	public void paths () {
		Array paths = new Array();
		Array parents = new Array();
		Array parents2 = new Array();
		{
			JsonMatcher matcher = new JsonMatcher();
			matcher.setProcessor(map -> {
				paths.add(matcher.path());
				parents.add(matcher.parent());
				parents2.add(matcher.parent(2));
			});
			matcher.addPattern("*/devices/*@/(serial_num,percentFull)");
			matcher.parse(json);
		}
		{
			JsonMatcher matcher = new JsonMatcher();
			matcher.addPattern("**@/(value)", map -> {
				paths.add(matcher.path());
				parents.add(matcher.parent());
				parents2.add(matcher.parent(2));
			});
			matcher.parse(json);
			matcher.parse("{a:{b:{c:{d:{e:{f:{value:deep}}}}}}}");
		}

		assertMapCount(6, paths);

		assertEquals("[]/{}/devices/{}", paths.first());
		assertEquals("[]/{}/devices/{}", paths.first());
		assertEquals("[]/{}/devices/{}", paths.get(1));
		assertEquals("[]/{}/devices/{}", paths.get(2));
		assertEquals("[]/{}/devices/{}/child", paths.get(3));
		assertEquals("[]/{}/devices/{}/child", paths.get(4));
		assertEquals("{}/a/b/c/d/e/f", paths.get(5));

		assertMapCount(6, parents);
		assertEquals("{}", parents.first());
		assertEquals("{}", parents.get(1));
		assertEquals("{}", parents.get(2));
		assertEquals("child", parents.get(3));
		assertEquals("child", parents.get(4));
		assertEquals("f", parents.get(5));

		assertMapCount(6, parents2);
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
		Array<ObjectMap> maps = new Array();
		matcher
			.addPattern("*/devices/*/(maxCellTemp,temperature,dc_switch_off,admin_state_str,sleep_enabled,device_status,object)");
		matcher.setProcessor(map -> copy(map, maps));
		matcher.parse(json);

		assertMapCount(1, maps);
		ObjectMap map = maps.first();
		assertTrue(map.get("maxCellTemp").getClass() == Long.class);
		assertTrue(map.get("temperature").getClass() == Double.class);
		assertTrue(map.containsKey("dc_switch_off"));
		assertTrue(map.get("dc_switch_off", "not this") == null);
		assertTrue(map.get("admin_state_str").getClass() == String.class);
		assertTrue(map.get("sleep_enabled").getClass() == Boolean.class);
		assertTrue(map.get("device_status").getClass() == Array.class);
		assertTrue(map.get("object").getClass() == ObjectMap.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern1 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("path/to/nowhere");
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern2 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("path()");
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern3 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("path(name");
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern4 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("path/name");
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern5 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a//b/c(value)"); // Double slash
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPattern6 () {
		JsonMatcher matcher = new JsonMatcher();
		matcher.addPattern("a//b/(c)"); // Double slash
	}

	static void test (String json, String pattern, String... expected) {
		test(null, json, new String[] {pattern}, expected);
	}

	static void test (String json, String[] patterns, String... expected) {
		test(null, json, patterns, expected);
	}

	static void test (@Null String notParsedValue, String json, String[] patterns, String... expected) {
		Array<ObjectMap> maps = new Array();
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
		matcher.setProcessor(map -> copy(map, maps));
		for (String pattern : patterns)
			matcher.addPattern(pattern);
		matcher.parse(json);
		try {
			assertMapCount(expected.length, maps);
			for (int i = 0, n = expected.length; i < n; i++)
				assertEquals("Pattern " + i, expected[i], maps.get(i).toString());
			if (notParsedValue != null && !ended[0]) fail("Should have ended but did not");
		} catch (AssertionError ex) {
			printResults(maps, json, patterns, expected);
			throw ex;
		}
	}

	static void printResults (Array<ObjectMap> maps, String json, String[] patterns, String... expected) {
		System.out.println("    JSON: " + json);
		if (patterns.length == 1)
			System.out.println(" Pattern: " + patterns[0]);
		else
			System.out.println("Patterns: " + Arrays.toString(patterns));
		System.out
			.println("Expected: " + expected.length + " " + Arrays.toString(expected).replace("\n", "\\n").replace("\t", "\\t"));
		System.out.println("  Actual: " + maps.size + " " + maps.toString().replace("\n", "\\n").replace("\t", "\\t"));
	}

	static void assertMapCount (int count, Array maps) {
		if (maps.size != count)
			System.out.println("Actual: " + maps.size + " " + maps.toString().replace("\n", "\\n").replace("\t", "\\t"));
		assertEquals("Wrong match count", count, maps.size);
	}

	static void copy (ObjectMap<String, Object> map, Array<ObjectMap> maps) {
		OrderedMap copy = new OrderedMap();
		copy.putAll(map);
		copy.orderedKeys().sort();
		maps.add(copy);
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

package com.example.demo;

import java.util.ArrayList;
// import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONObject;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.Expression;
import net.thisptr.jackson.jq.Function;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.PathOutput;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Version;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import net.thisptr.jackson.jq.internal.misc.Strings;
import net.thisptr.jackson.jq.module.ModuleLoader;
import net.thisptr.jackson.jq.module.loaders.BuiltinModuleLoader;
import net.thisptr.jackson.jq.module.loaders.ChainedModuleLoader;
import net.thisptr.jackson.jq.module.loaders.FileSystemModuleLoader;
import net.thisptr.jackson.jq.path.Path;
import com.example.demo.MyData;

import io.burt.jmespath.JmesPath;
// import io.burt.jmespath.Expression;
import io.burt.jmespath.jackson.*;

import com.octomix.josson.*;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class controller {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	// Josson.setLocale(Locale.ENGLISH); // default Locale.getDefault()

	// Josson.setZoneId(ZoneId.of("Asia/Hong_Kong")); // default
	// ZoneId.systemDefault()

	// Josson.setSerializationInclusion(JsonInclude.Include.NON_NULL);

	@RequestMapping(value = "/sowhatpath", method = RequestMethod.POST)
	public ResponseEntity<Object> pathhh(@RequestBody MyData json) {

		Object dataObject = JsonPath.parse(json.getData()).read(json.getPath());

		return new ResponseEntity<>(dataObject, HttpStatus.OK);

	}

	@RequestMapping(value = "/sowhatjooo", method = RequestMethod.POST)
	public ResponseEntity<JsonNode> joooo(@RequestBody MyData json) {

		Josson josson = Josson.create(json.getData());
		JsonNode result = josson.getNode(json.getPath());
		return new ResponseEntity<>(result, HttpStatus.OK);

	}

	@RequestMapping(value = "/sowhatmesh", method = RequestMethod.POST)
	public ResponseEntity<JsonNode> jmesh(@RequestBody MyData json) {

		// RuntimeConfiguration configuration = new
		// RuntimeConfiguration.Builder().withSilentTypeErrors(true).build();

		JmesPath<JsonNode> jmespath = new JacksonRuntime();

		io.burt.jmespath.Expression<JsonNode> expression = jmespath
				.compile(json.getPath()); // new schema from db

		JsonNode input = json.getData(); // from workflow
		JsonNode result = expression.search(input);
		// JsonNode result = null;
		return new ResponseEntity<>(result, HttpStatus.OK);

	}

	// @PostMapping("/sowhat")
	// @GetMapping("/tutorials")
	// @RequestBody
	@RequestMapping(value = "/sowhat", method = RequestMethod.POST)
	public ResponseEntity<List<JsonNode>> getAllTutorials(@RequestBody MyData json) {
		// public ResponseEntity<List<JsonNode>> getAllTutorials(@RequestParam(required
		// = false) String title) {
		try {
			String tutorials = "new ArrayList()";

			// First of all, you have to prepare a Scope which s a container of
			// built-in/user-defined functions and variables.
			Scope rootScope = Scope.newEmptyScope();

			// Use BuiltinFunctionLoader to load built-in functions from the classpath.
			BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);

			// You can also define a custom function. E.g.
			rootScope.addFunction("repeat", 1, new Function() {
				@Override
				public void apply(Scope scope, List<Expression> args, JsonNode in, Path path, PathOutput output,
						Version version) throws JsonQueryException {
					args.get(0).apply(scope, in, (time) -> {
						output.emit(new TextNode(Strings.repeat(in.asText(), time.asInt())), null);
					});
				}
			});

			// For import statements to work, you need to set ModuleLoader.
			// BuiltinModuleLoader uses ServiceLoader mechanism to
			// load Module implementations.
			rootScope.setModuleLoader(BuiltinModuleLoader.getInstance());

			// Alternatively, you can also use/combine FileSystemModuleLoader to load
			// .jq/.json files from the file system.
			// rootScope.setModuleLoader(new ChainedModuleLoader(new ModuleLoader[] {
			// BuiltinModuleLoader.getInstance(),
			// new FileSystemModuleLoader(rootScope, Versions.JQ_1_6,
			// FileSystems.getDefault().getPath("").toAbsolutePath(), // search modules in
			// the actual file system
			// Paths.get(Scope.class.getClassLoader().getResource("classpath_modules").toURI())),
			// // or in the classpath resources
			// }));

			// After this initial setup, rootScope should not be modified (via
			// Scope#setValue(...),
			// Scope#addFunction(...), etc.) so that it can be shared (in a read-only
			// manner) across mutliple threads
			// because you want to avoid heavy lifting of loading built-in functions every
			// time which involves
			// file system operations and a lot of parsing.

			// Instead of modifying the rootScope directly, you can create a child Scope.
			// This is especially useful when
			// you want to use variables or functions that is only local to the specific
			// execution context (such as
			// a thread, request, etc).
			// Creating a child Scope is a very light-weight operation that just allocates a
			// Scope and sets
			// one of its fields to point to the given parent scope. It's totally okay to
			// create a child Scope
			// per every apply() invocations if you need to do so.
			Scope childScope = Scope.newChildScope(rootScope);

			// Scope#setValue(...) sets a custom variable that can be used from jq
			// expressions. This variable is local to the
			// childScope and cannot be accessed from the rootScope. The rootScope will not
			// be modified by this call.
			childScope.setValue("param", IntNode.valueOf(42));

			// JsonQuery#compile(...) parses and compiles a given expression. The resulting
			// JsonQuery instance
			// is immutable and thread-safe. It should be reused as possible if you
			// repeatedly use the same expression.
			// JsonQuery q = JsonQuery.compile("$param * 2", Versions.JQ_1_6);
			JsonQuery q = JsonQuery.compile(json.getPath(), Versions.JQ_1_6);

			// { key3: (.key1.value1 + .key2.value2) }
			// You need a JsonNode to use as an input to the JsonQuery. There are many ways
			// you can grab a JsonNode.
			// In this example, we just parse a JSON text into a JsonNode.
			// JsonNode in =
			// MAPPER.readTree("{\"ids\":\"12,15,23\",\"hi\":\"33\",\"timestamp\":1418785331123}");
			JsonNode in = json.getData();// MAPPER.readTree(json.getData());

			// Finally, JsonQuery#apply(...) executes the query with given input and
			// produces 0, 1 or more JsonNode.
			// The childScope will not be modified by this call because it internally
			// creates a child scope as necessary.
			final List<JsonNode> out = new ArrayList<>();
			q.apply(childScope, in, out::add);

			// final JqResponse response = request.execute();
			// if (response.hasErrors()) {
			// // display errors in response.getErrors()
			// } else {
			// System.out.println("JQ output: " + response.getOutput());
			// }

			return new ResponseEntity<>(out, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
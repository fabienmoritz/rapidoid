package org.rapidoid.docs;

/*
 * #%L
 * rapidoid-docs
 * %%
 * Copyright (C) 2014 - 2015 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.util.List;
import java.util.Map;

import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.app.AppHandler;
import org.rapidoid.config.Config;
import org.rapidoid.ctx.Classes;
import org.rapidoid.http.HTTP;
import org.rapidoid.http.HttpException;
import org.rapidoid.io.IO;
import org.rapidoid.main.Rapidoid;
import org.rapidoid.plugins.templates.Templates;
import org.rapidoid.scan.ClasspathUtil;
import org.rapidoid.util.U;
import org.rapidoid.util.UTILS;
import org.rapidoid.webapp.AppMode;
import org.rapidoid.webapp.WebApp;
import org.rapidoid.webapp.WebAppGroup;
import org.rapidoid.widget.SnippetWidget;
import org.rapidoid.wrap.IntWrap;

@Authors("Nikolche Mihajlovski")
@Since("4.1.0")
public class Docs {

	public static void main(String[] args) {
		// Log.setLogLevel(LogLevel.DEBUG);
		ClasspathUtil.setIgnoreRapidoidClasses(false);
		Rapidoid.run(args);

		String path = "../../rapidoid.github.io/";
		U.must(new File(path).exists());

		generateIndex(path);

		System.exit(0);
	}

	private static void generateIndex(String path) {
		System.out.println();
		System.out.println();
		System.out.println("*************** " + path);
		System.out.println();
		System.out.println();

		List<Map<String, ?>> examples = U.list();

		IntWrap nn = new IntWrap();

		List<String> eglist = IO.loadLines("examples.txt");
		List<String> processed = processAll(examples, nn, eglist);

		Map<String, ?> model = U.map("examples", examples);
		String html = Templates.fromFile("docs.html").render(model);
		IO.save(path + "index.html", html);
	}

	private static List<String> processAll(List<Map<String, ?>> examples, IntWrap nn, List<String> eglist) {
		List<String> processed = U.list();

		for (String eg : eglist) {
			eg = eg.trim();

			String[] parts = eg.split("\\:");
			if (parts.length == 2 && !eg.startsWith("#")) {
				processIndex(nn, examples, parts[0].trim(), parts[1].trim());
			}
		}

		return processed;
	}

	private static void processIndex(IntWrap nn, List<Map<String, ?>> examples, String id, String title) {
		System.out.println();
		System.out.println();
		System.out.println(id);
		System.out.println();
		System.out.println();

		List<String> javaFiles = U.list();
		List<String> resFiles = U.list();

		IO.findAll(new File("src/main/java/org/rapidoid/docs/" + id), javaFiles);
		IO.findAll(new File("src/main/java/org/rapidoid/docs/" + id), resFiles);

		List<?> snippets = snippets(id, javaFiles, resFiles);

		Iterable<Class<?>> clss = ClasspathUtil.scanClasses("org.rapidoid.docs." + id, null, null, null, null);

		Classes classes = Classes.from(clss);
		Config config = new Config();
		WebApp app = new WebApp(id, null, null, U.set("/"), AppMode.PRODUCTION, null, null, classes, config);
		app.getRouter().generic(new AppHandler());

		WebAppGroup.main().clear();
		WebAppGroup.main().setDefaultApp(app);

		List<?> results = getResults(id);

		// title += " :: " + id;
		nn.value++;
		String desc = "desc";
		Map<String, ?> model = U.map("n", id, "nn", nn.value, "snippets", snippets, "title", title, "desc", desc,
				"results", results);
		examples.add(model);

		WebAppGroup.main().setDefaultApp(null);
	}

	private static List<?> snippets(String id, List<String> files, List<String> resFiles) {
		List<Object> snippets = U.list();

		for (String file : files) {
			String snippet = IO.load(file);

			snippet = cleanSnippet(snippet);

			snippet = SnippetWidget.prettify(snippet, true);

			String desc = new File(file).getName();
			snippets.add(U.map("desc", desc, "code", snippet));
		}

		return snippets;
	}

	private static List<?> getResults(String id) {
		List<String> lines = IO.loadLines("tests/" + id + ".txt");

		List<Object> results = U.list();

		if (lines == null) {
			lines = U.list("GET /");
		}

		if (lines != null) {
			for (String line : lines) {
				Map<String, ?> res = getResult(line);
				results.add(res);
			}
		}

		return results;
	}

	private static Map<String, String> getResult(String line) {
		String[] parts = line.split("\\s", 2);
		String verb = parts[0];
		String uri = parts[1];

		String uri2 = uri.contains("?") ? uri.replace("?", "?_embedded=true&") : uri + "?_embedded=true";

		String result = null;

		try {
			if ("GET".equalsIgnoreCase(verb)) {
				result = new String(HTTP.get("http://localhost:8080" + uri2));
			} else if ("POST".equalsIgnoreCase(verb)) {
				result = new String(HTTP.post("http://localhost:8080" + uri2, null, null, null));
			}
		} catch (Exception e) {
			Throwable cause = UTILS.rootCause(e);
			if (cause instanceof HttpException && cause.getMessage().contains("404")) {
				result = "<span class=\"not-found\">404 Not found!</span>";
			} else {
				e.printStackTrace();
			}
		}

		if (result == null) {
			result = nonHttpResult(line);
		}

		return U.map("verb", verb, "uri", uri, "result", result);
	}

	private static String nonHttpResult(String line) {
		return "?";
	}

	private static String cleanSnippet(String s) {
		String comm = "#L%\n */";
		int p = s.indexOf(comm);
		U.must(p > 0);
		s = s.substring(p + comm.length()).trim();
		return s;
	}

}

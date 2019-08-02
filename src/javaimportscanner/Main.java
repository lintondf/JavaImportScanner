/**
 * 
 */
package javaimportscanner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javaimportscanner.antlr4.Java9BaseListener;
import javaimportscanner.antlr4.Java9Lexer;
import javaimportscanner.antlr4.Java9Parser;
import javaimportscanner.antlr4.Java9Parser.ImportDeclarationContext;
import javaimportscanner.antlr4.JavaLexer;
import javaimportscanner.antlr4.JavaParser;
import javaimportscanner.antlr4.JavaParserBaseListener;

/**
 * @author lintondf
 *
 */
public class Main {
	
	protected TreeMap<String, TreeSet<String>> imports = new TreeMap<>();
	protected String currentSource;
	
	public class JavaListener extends JavaParserBaseListener {

		@Override
		public void enterImportDeclaration(javaimportscanner.antlr4.JavaParser.ImportDeclarationContext ctx) {
			String importPackage = ctx.getText().replace("import", "").replace(";", "");
			TreeSet<String> usages = imports.get(importPackage);
			if (usages == null) {
				usages = new TreeSet<>();
				usages.add(currentSource);
				imports.put(importPackage, usages);
			} else {
				usages.add(currentSource);
			}
		}
		
	}
	
	
	protected void scanFile( File src ) {
		try {
			currentSource = src.getAbsolutePath();
			byte[] bytes = Files.readAllBytes(src.toPath());
			String content = new String(bytes, "UTF-8");
			
			JavaLexer lexer = new JavaLexer(CharStreams.fromString(content));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			JavaParser parser = new JavaParser( tokens );
			
			ParseTree tree = parser.compilationUnit();
			
			JavaListener listener = new JavaListener();
			ParseTreeWalker walker = new ParseTreeWalker();
			walker.walk(listener, tree);			
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	protected void findSource( File d ) {
		System.out.println(d.getAbsolutePath());
		for (File f : d.listFiles()) {
			if (f.getName().startsWith(".") )
				continue;
			if (f.isFile() && f.getName().endsWith(".java")) {
				scanFile(f);
			}
			if (f.isDirectory()) {
				findSource(f);
			}
		}		
	}
	

	
	public Main( String[] args) {
		if (args == null || args.length == 0)
			args = new String[] {"/Users/lintondf/GITHUB/MorrisonPolynomialFiltering/Tools/src"};
		Path base = Paths.get(args[0]);
		findSource(base.toFile());
		
		for (String importPackage : imports.keySet()) {
			System.out.println(importPackage);
			TreeSet<String> usages = imports.get(importPackage);
			for (String where :usages ) {
				System.out.println("  " + where.replace(base.toString(), "."));
			}
		}
	}
 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main main = new Main( args );
	}

}

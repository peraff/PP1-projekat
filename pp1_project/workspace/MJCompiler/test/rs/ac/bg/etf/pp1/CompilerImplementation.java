package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.ac.bg.etf.pp1.test.Compiler;
import rs.ac.bg.etf.pp1.test.CompilerError;

public class CompilerImplementation implements Compiler{
	
	
	public static final Struct booldType = new Struct(Struct.Bool);
	public static final Compiler myCompilerImpl = new CompilerImplementation();
	public static final Logger log = Logger.getLogger(CompilerImplementation.class);
	
	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			log.error("Not enough arguments supplied! Usage: MJParser <source-file> <obj-file> ");
			return;
		}
		myCompilerImpl.compile(args[0], args[1]);
	}

	@Override
	public List<CompilerError> compile(String sourceFilePath, String outputFilePath) {
		
		Logger log = Logger.getLogger(CompilerImplementation.class);
		File sourceCode = new File(sourceFilePath);
		List<CompilerError> l = new ArrayList<CompilerError>();
		
		if (!sourceCode.exists()) {
			log.error("Source file [" + sourceCode.getAbsolutePath() + "] not found!");
			return null;
		}
			
		log.info("Compiling source file: " + sourceCode.getAbsolutePath());
		
		try (BufferedReader br = new BufferedReader(new FileReader(sourceCode))) {
			Yylex lexer = new Yylex(br);
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
	        SyntaxNode prog = (SyntaxNode)(s.value);
	        log.info(prog.toString());
			Tab.init(); // Universe scope

			//INSERT BOOL TYPE INTO SYMBOL TABLE
			Tab.currentScope.addToLocals(new Obj(Obj.Type, "bool", booldType));
			
			l.addAll(lexer.llist);
			l.addAll(p.plist);
			SemanticAnalyzer semanticCheck = new SemanticAnalyzer(l);
			prog.traverseBottomUp(semanticCheck);
			
	        //log.info("Print calls = " + semanticCheck.prints);
	        Tab.dump();
	        
	        
	        if(l.isEmpty()) { 
	        	//log.info("Parsiranje uspesno zavrseno!!");
	        	
	        	
	        	File objFile = new File(outputFilePath);
	        	log.info("Generating bytecode file: " + objFile.getAbsolutePath());
	        	if (objFile.exists())
	        		objFile.delete();
	        	
	        	// Code generation...
	        	CodeGenerator codeGenerator = new CodeGenerator();
	        	prog.traverseBottomUp(codeGenerator);
	        	Code.dataSize = semanticCheck.nVars;
	        	Code.mainPc = codeGenerator.getMainPc();
	        	Code.write(new FileOutputStream(objFile));
	        	log.info("Parsiranje uspesno zavrseno!");
	        	
	        	return l;
	        } else { 
	        	log.error("Neuspesno parsiranje programa!\nLista gresaka:");
	        	for(CompilerError ce : l) { 
	        		log.error(ce.toString() + '\n');
	        	}
	        }
	        
	        
	        /*
	        if (!p.errorDetected && semanticCheck.passed()) {
	        	log.info("SEMANTIC PASSED!");
	        	
	        	/*
	        	File objFile = new File(args[1]);
	        	log.info("Generating bytecode file: " + objFile.getAbsolutePath());
	        	if (objFile.exists())
	        		objFile.delete();
	        	
	        	// Code generation...
	        	CodeGenerator codeGenerator = new CodeGenerator();
	        	prog.traverseBottomUp(codeGenerator);
	        	Code.dataSize = semanticCheck.nVars;
	        	Code.mainPc = codeGenerator.getMainPc();
	        	Code.write(new FileOutputStream(objFile));
	        	log.info("Parsiranje uspesno zavrseno!");
	        	
	        ///////////////////////////////////// DODAJ OVDE Kraj komentara
	        	return null;
	        }
	        else {
	        	log.error("Parsiranje NIJE uspesno zavrseno!");
	        }
	        */
		} catch(Exception e) { 
			e.printStackTrace();
		}

		return null;
	}

}

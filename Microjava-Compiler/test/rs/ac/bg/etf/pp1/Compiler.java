package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;


public class Compiler {

	
	
	public static void main(String[] args) throws Exception {
		Reader br = null;
		try {
			
			File sourceCode = new File("test/program.mj");
			System.out.println("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
	        
	        Program prog = (Program)(s.value);
	        
	        System.out.println(prog.toString());
	        
	        
	        Tab.init();
	        Tab.insert(Obj.Type, "bool", new Struct(Struct.Bool));
	        
	        Code.put(Code.return_);
	        
	        SemanticAnalyzer analyzer = new SemanticAnalyzer();
	        
	        prog.traverseBottomUp(analyzer);
	        
	        System.out.println(" Declared variables count = " + analyzer.varDeclCount);
	        Tab.dump();
	        
	        if(!p.errorDetected && !analyzer.errorDetected) {
	        	File objFile = new File("test/program.obj");
				if(objFile.exists()) objFile.delete();
				
				CodeGenerator codeGenerator = new CodeGenerator();
				prog.traverseBottomUp(codeGenerator);
				Code.dataSize = analyzer.nvars;
				Code.mainPc = codeGenerator.getMainPc();
				Code.write(new FileOutputStream(objFile));
	        	
	        	System.out.println("Uspesno parsiranje!");
	        	
	        	//Tab.dump();
	        }else {
	        	System.err.println("Neuspesno parsiranje!");
	        }
	        
	        
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { 
				System.out.println(e1.getMessage()); 
				System.out.println(e1); 
			}
		}
	}
	
}

package rs.ac.bg.etf.pp1;


import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.pp1.ast.FormParArr;
import rs.ac.bg.etf.pp1.ast.FormParIdent;
import rs.ac.bg.etf.pp1.ast.FormParamLArr;
import rs.ac.bg.etf.pp1.ast.FormParamLIdent;
import rs.ac.bg.etf.pp1.ast.FormParsList;
import rs.ac.bg.etf.pp1.ast.FormalParameters;
import rs.ac.bg.etf.pp1.ast.VarArrayIdent;
import rs.ac.bg.etf.pp1.ast.VarArrayLIdent;
import rs.ac.bg.etf.pp1.ast.VarDeclCIdent;
import rs.ac.bg.etf.pp1.ast.VarDeclIdent;
import rs.ac.bg.etf.pp1.ast.VarDeclList;
import rs.ac.bg.etf.pp1.ast.VarDeclarations;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CounterVisitor extends VisitorAdaptor {

	protected int count;
	
	public int getCount(){
		return count;
	}
	
	public static class FormParamCounter extends CounterVisitor{
		@Override
		public void visit(FormalParameters formalParameters) {
			
			FormParsList formParsList= formalParameters.getFormParsList();
			
			while(formParsList instanceof FormParamLIdent || formParsList instanceof FormParamLArr) {
				if(formParsList instanceof FormParamLIdent) {
					FormParamLIdent formParamLIdent = (FormParamLIdent)formParsList;
					formParsList = formParamLIdent.getFormParsList();
				}else if(formParsList instanceof FormParamLArr) {
					FormParamLArr formParamLArr = (FormParamLArr)formParsList;
					formParsList = formParamLArr.getFormParsList();
				}
				
				count++;
			}
			
			if(formParsList instanceof FormParIdent || formParsList instanceof FormParArr) {
				count++;
			}
		}
		
	}
	
	public static class VarCounter extends CounterVisitor{
		
		@Override
		public void visit(VarDeclarations varDeclarations) {
			
			VarDeclList varDeclList= varDeclarations.getVarDeclList();
			
			while(varDeclList instanceof VarDeclCIdent || varDeclList instanceof VarArrayLIdent) {
				if(varDeclList instanceof VarDeclCIdent) {
					VarDeclCIdent varDeclCIdent = (VarDeclCIdent)varDeclList;
					varDeclList = varDeclCIdent.getVarDeclList();
				}else if(varDeclList instanceof VarArrayLIdent) {
					VarArrayLIdent varArrayLIdent = (VarArrayLIdent)varDeclList;
					varDeclList = varArrayLIdent.getVarDeclList();
				}
				
				count++;
			}
			
			if(varDeclList instanceof VarDeclIdent || varDeclList instanceof VarArrayIdent) {
				count++;
			}
			
		} 
	}
}

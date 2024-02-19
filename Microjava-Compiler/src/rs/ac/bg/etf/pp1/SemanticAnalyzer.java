package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor{
	boolean errorDetected = false, returnFound = false;
	Obj currentMethod = null;
	int varDeclCount = 0;
	int nvars = 0;
	String curNamespace = "";
	
	
	public void report_error(String message, SyntaxNode info) {
		errorDetected=true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		System.err.println(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		System.out.println(msg.toString());
	}

	
	
	@Override
	public void visit(MethodTypeNameType methodTypeName) {
		String methName = curNamespace==""?methodTypeName.getMethName():curNamespace+"::"+methodTypeName.getMethName();
		currentMethod = Tab.insert(Obj.Meth, methName, methodTypeName.getType().struct);
		methodTypeName.obj = currentMethod;
		Tab.openScope();
		report_info("Obradjuje se funkcija " + methName, methodTypeName);
	}
	
	@Override
	public void visit(MethodTypeNameVoid methodTypeNameVoid) {
		String methName = curNamespace==""?methodTypeNameVoid.getMethName():curNamespace+"::"+methodTypeNameVoid.getMethName();
		currentMethod = Tab.insert(Obj.Meth, methName, Tab.noType);
		methodTypeNameVoid.obj = currentMethod;
		Tab.openScope();
		report_info("Obradjuje se funkcija " + methName, methodTypeNameVoid);
	}
	
	@Override
	public void visit(TermExpr termExpr) {
		termExpr.struct = termExpr.getTerm().struct;
	}
	
	@Override
	public void visit(MinusTermExpr minusTermExpr) {
		minusTermExpr.struct = minusTermExpr.getTerm().struct;
	}
	
	@Override
	public void visit(AddopExpr addopExpr) {
		System.out.println(addopExpr.getAddop());
		Struct te = addopExpr.getExpr().struct;
		Struct t = addopExpr.getTerm().struct;
		
		if(te.equals(t) && te == Tab.intType) {
			addopExpr.struct = te;
		}else {
			report_error("Greska na liniji "+ addopExpr.getLine()+" : nekompatibilni tipovi u izrazu za sabiranje.", null);
			addopExpr.struct = Tab.noType;
		}
	}
	
	
	@Override
	public void visit(TermFactor termFactor) {
		
		termFactor.struct = termFactor.getFactor().struct;
	}
	
	@Override
	public void visit(TermMulop termMulop) {
		Struct t1 = termMulop.getFactor().struct;
		Struct t2 = termMulop.getTerm().struct;
		
		if(t1.equals(t2) && t1 == Tab.intType) {
			termMulop.struct = t1;
		}else {
			report_error("Greska na liniji "+ termMulop.getLine()+" : nekompatibilni tipovi u izrazu za mnozenje.", null);
			termMulop.struct = Tab.noType;
		}
	}
	
	
	@Override
	public void visit(FactorNumber factorNumber) {
		factorNumber.struct = Tab.intType;
	}
	
	@Override
	public void visit(FactorChar factorChar) {
		factorChar.struct = Tab.charType;
	}
	
	@Override
	public void visit(FactorBool factorBool) {
		factorBool.struct =  Tab.find("bool").getType();
	}
	
	@Override
	public void visit(FactorDesignator factorDesignator) {
		if(factorDesignator.getDesignator().getClass() == DesignatorVar.class || factorDesignator.getDesignator().getClass() == DesignatorNmspcVar.class) {
			factorDesignator.struct = factorDesignator.getDesignator().obj.getType();
		}else if(factorDesignator.getDesignator().getClass() == DesignatorArr.class || factorDesignator.getDesignator().getClass() == DesignatorNmspcArr.class) {
			factorDesignator.struct = factorDesignator.getDesignator().obj.getType();
		}
		
	}
	
	
	@Override
	public void visit(DesignatorVar designatorVar) {
		String name = designatorVar.getName();
		if(! curNamespace.equals("")) {
			name = curNamespace+"::"+name;
		}
    	Obj obj = Tab.find(name);
    	if(obj == Tab.noObj){
			report_error("Greska na liniji " + designatorVar.getLine()+ " : ime "+designatorVar.getName()+" nije deklarisano! ", null);
    	}
    	
    	designatorVar.obj = obj;
	}
	
	@Override
	public void visit(DesignatorArr designatorArr) {
		String name = ((ArrayStart)designatorArr.getArrStart()).getName();
		if(! curNamespace.equals("")) {
			name = curNamespace+"::"+name;
		}
    	Obj obj = Tab.find(((ArrayStart)designatorArr.getArrStart()).getName());
    	if(obj == Tab.noObj){
			report_error("Greska na liniji " + designatorArr.getLine()+ " : ime "+((ArrayStart)designatorArr.getArrStart()).getName()+" nije deklarisano! ", null);
    	}
    	designatorArr.obj = new Obj(Obj.Elem, "", designatorArr.getArrStart().obj.getType().getElemType());
	}
	
	@Override
	public void visit(DesignatorNmspcVar designatorNmspcVar) {
    	Obj obj = Tab.find(((Namespace_Name)designatorNmspcVar.getNamespaceName()).getNmpscName() + "::" + designatorNmspcVar.getName());
    	if(obj == Tab.noObj){
			report_error("Greska na liniji " + designatorNmspcVar.getLine()+ " : ime "+designatorNmspcVar.getName()+" nije deklarisano! ", null);
    	}
    	designatorNmspcVar.obj = obj;
	}
	
	@Override
	public void visit(DesignatorNmspcArr designatorNmspcArr) {
    	Obj obj = Tab.find(((Namespace_Name)designatorNmspcArr.getNamespaceName()).getNmpscName() + "::" + ((ArrayStart)designatorNmspcArr.getArrStart()).getName());
    	if(obj == Tab.noObj){
			report_error("Greska na liniji " + designatorNmspcArr.getLine()+ " : ime "+((ArrayStart)designatorNmspcArr.getArrStart()).getName()+" nije deklarisano! ", null);
    	}
    	designatorNmspcArr.obj = new Obj(Obj.Elem, "", designatorNmspcArr.getArrStart().obj.getType().getElemType());
	}
	
	@Override
	public void visit(ArrayStart arrayStart) {
		if(arrayStart.getParent().getClass() == DesignatorArr.class) {
	    	Obj obj = Tab.find(arrayStart.getName());
	    	
	    	arrayStart.obj = obj;
		}else if(arrayStart.getParent().getClass() == DesignatorNmspcArr.class) {
			DesignatorNmspcArr designatorNmspcArr = (DesignatorNmspcArr)arrayStart.getParent();
			Obj obj = Tab.find(((Namespace_Name)designatorNmspcArr.getNamespaceName()).getNmpscName() + "::" + arrayStart.getName());
	    	
	    	arrayStart.obj = obj;
		}
	}
	
	
	
	
	@Override
	public void visit(ReturnStmt returnStmt) {
    	returnFound = true;
    	Struct currMethType = currentMethod.getType();
    	if(!currMethType.compatibleWith(returnStmt.getExpr().struct)){
			report_error("Greska na liniji " + returnStmt.getLine() + " : " + "tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije " + currentMethod.getName(), null);
    	}
	}
	
	@Override
    public void visit(MethodDecl methodDecl){
    	if(!returnFound && currentMethod.getType() != Tab.noType){
			report_error("Semanticka greska na liniji " + methodDecl.getLine() + ": funkcija " + currentMethod.getName() + " nema return iskaz!", null);
    	}
    	//Tab.dump();
    	Tab.chainLocalSymbols(currentMethod);
    	Tab.closeScope();
    	
    	returnFound = false;
    	currentMethod = null;
    }
	
	
	
	@Override
	public void visit(TypeIdent typeIdent) {
		Obj typeNode = Tab.find(typeIdent.getTypeName());
		if(typeNode == Tab.noObj) {
    		report_error("Nije pronadjen tip " + typeIdent.getTypeName() + " u tabeli simbola! ", null);
    		typeIdent.struct = Tab.noType;
		}else {
    		if(Obj.Type == typeNode.getKind()){
    			typeIdent.struct = typeNode.getType();
    		}else{
    			report_error("Greska: Ime " + typeIdent.getTypeName() + " ne predstavlja tip!", typeIdent);
    			typeIdent.struct = Tab.noType;
    		}
		}
	}
	
	
	
	@Override
	public void visit(VarDeclIdent varDeclIdent) {
	}
	
	
	@Override
	public void visit(VarDeclCIdent varDeclCIdent) {
		
	}

	
	@Override
	public void visit(ProgramName programName) {
		programName.obj = Tab.insert(Obj.Prog, programName.getProgName(), Tab.noType);
    	Tab.openScope();
	}
	
	@Override
	public void visit(Program_ program_) {
    	nvars = Tab.currentScope.getnVars();
    	Tab.chainLocalSymbols(program_.getProgName().obj);
    	Tab.closeScope();
	}
	
	@Override
	public void visit(Assignment assignment) {
		if(assignment.getDesignator().getClass() == DesignatorVar.class) {
			if(! assignment.getExpr().struct.assignableTo(assignment.getDesignator().obj.getType()))
				report_error("Greska na liniji " + assignment.getLine() + " : " + "nekompatibilni tipovi u dodeli vrednosti! ", null);
		}else if(assignment.getDesignator().getClass() == DesignatorArr.class) {
			
		}
	}
	
	
	
	 public void visit(PrintStmt print) {
	    	if(print.getExpr().struct != Tab.intType && print.getExpr().struct!= Tab.charType && print.getExpr().struct!=Tab.find("bool").getType())
	    			report_error ("Semanticka greska na liniji " + print.getLine() + ": Operand instrukcije PRINT mora biti char ili int tipa", null );
			
	}
	 
	 
	@Override
	public void visit(VarDeclarations varDeclarations) {
		List<String> var_names = new ArrayList<>();
		List<Boolean> is_array = new ArrayList<>();
		Struct varType = varDeclarations.getType().struct;
		
		VarDeclList varDeclList= varDeclarations.getVarDeclList();
		
		while(varDeclList.getClass() ==  VarDeclCIdent.class || varDeclList.getClass() == VarArrayLIdent.class) {
			if(varDeclList.getClass() ==  VarDeclCIdent.class) {
				VarDeclCIdent varDeclCIdent = (VarDeclCIdent)varDeclList;
				varDeclList = varDeclCIdent.getVarDeclList();
				if(curNamespace.equals("")) {
					var_names.add(0, varDeclCIdent.getVarName());
				}else {
					var_names.add(0, curNamespace+"::"+ varDeclCIdent.getVarName());
				}
				is_array.add(0, false);
			}else if(varDeclList.getClass() == VarArrayLIdent.class) {
				VarArrayLIdent varArrayLIdent = (VarArrayLIdent)varDeclList;
				if(curNamespace.equals("")) {
					var_names.add(0, varArrayLIdent.getArrName());
				}else {
					var_names.add(0, curNamespace+"::"+ varArrayLIdent.getArrName());
				}
				is_array.add(0, true);
				varDeclList=varArrayLIdent.getVarDeclList();
			}
			
		}
		
		if(varDeclList.getClass() ==  VarDeclIdent.class || varDeclList.getClass()==VarArrayIdent.class) {
			if(varDeclList.getClass() ==  VarDeclIdent.class) {
				VarDeclIdent varDeclIdent = (VarDeclIdent)varDeclList;
				if(curNamespace.equals("")) {
					var_names.add(0, varDeclIdent.getVarName());
				}else {
					var_names.add(0, curNamespace+"::"+ varDeclIdent.getVarName());
				}
				is_array.add(0, false);
			}else if(varDeclList.getClass()==VarArrayIdent.class) {
				VarArrayIdent varArrayIdent = (VarArrayIdent)varDeclList;
				if(curNamespace.equals("")) {
					var_names.add(0, varArrayIdent.getArrName());
				}else {
					var_names.add(0, curNamespace+"::"+ varArrayIdent.getArrName());
				}
				is_array.add(0, true);
			}

		}
		
		for(int i=0; i<var_names.size(); i++) {
			varDeclCount++;
			if(is_array.get(i)) {
				report_info("Deklarisan niz "+ var_names.get(i), varDeclList);
				Obj varNode = Tab.insert(Obj.Var, var_names.get(i), new Struct(Struct.Array, varType));
			}else {
				report_info("Deklarisana promenljiva "+ var_names.get(i), varDeclList);
				Obj varNode = Tab.insert(Obj.Var,var_names.get(i), varType);
			}

		}
		
	} 
	
	
	@Override
	public void visit(ConstDeclarations constDeclarations) {
		List<String> var_names = new ArrayList<>();
		List<Integer> var_values = new ArrayList<>();
		Struct varType = constDeclarations.getType().struct;
		
		ConstDeclList constDeclList= constDeclarations.getConstDeclList();
		
		while(constDeclList.getClass() == ConstDeclLNum.class || constDeclList.getClass() == ConstDeclLChar.class|| constDeclList.getClass() == ConstDeclLBool.class) {
			if(constDeclList.getClass() ==  ConstDeclLNum.class) {
				if(varType.getKind() != Struct.Int)
					report_error("Greska: Nekompatibilni tipovi u dodeli vrednosti konstanti ", constDeclList);
				ConstDeclLNum constDeclLNum = (ConstDeclLNum)constDeclList;
				constDeclList = constDeclLNum.getConstDeclList();
				if(curNamespace.equals("")) {
					var_names.add(0, constDeclLNum.getVarName());
				}else {
					var_names.add(0, curNamespace+"::"+ constDeclLNum.getVarName());
				}
				var_values.add(0, constDeclLNum.getNum1());
			}else if(constDeclList.getClass() ==  ConstDeclLChar.class) {
				if(varType.getKind() != Struct.Char)
					report_error("Greska: Nekompatibilni tipovi u dodeli vrednosti konstanti ", constDeclList);
				ConstDeclLChar constDeclLChar = (ConstDeclLChar)constDeclList;
				constDeclList = constDeclLChar.getConstDeclList();
				if(curNamespace.equals("")) {
					var_names.add(0, constDeclLChar.getVarName());
				}else {
					var_names.add(0, curNamespace+"::"+ constDeclLChar.getVarName());
				}
				var_values.add(0, constDeclLChar.getChar1().charAt(1) + 0);
			}else if(constDeclList.getClass() ==  ConstDeclLBool.class) {
				if(varType.getKind() != Struct.Bool)
					report_error("Greska: Nekompatibilni tipovi u dodeli vrednosti konstanti ", constDeclList);
				ConstDeclLBool constDeclLBool = (ConstDeclLBool)constDeclList;
				constDeclList = constDeclLBool.getConstDeclList();
				if(curNamespace.equals("")) {
					var_names.add(0, constDeclLBool.getVarName());
				}else {
					var_names.add(0, curNamespace+"::"+ constDeclLBool.getVarName());
				}
				int boolValue = 0;
				if(constDeclLBool.getBool().getClass() == BoolTrue.class) {
					boolValue=1;
				}else if(constDeclLBool.getBool().getClass() == BoolFalse.class) {
					boolValue=0;
				}
				var_values.add(0, '0' + boolValue);
			}
			
		}
		
		if(constDeclList.getClass() == ConstDeclNum.class || constDeclList.getClass() == ConstDeclChar.class|| constDeclList.getClass() == ConstDeclBool.class) {
			if(constDeclList.getClass() ==  ConstDeclNum.class) {
				if(varType.getKind() != Struct.Int)
					report_error("Greska: Nekompatibilni tipovi u dodeli vrednosti konstanti ", constDeclList);
				ConstDeclNum constDeclNum = (ConstDeclNum)constDeclList;
				if(curNamespace.equals("")) {
					var_names.add(0, constDeclNum.getVarName());
				}else {
					var_names.add(0, curNamespace+"::"+ constDeclNum.getVarName());
				}
				var_values.add(0, constDeclNum.getNum2());
			}else if(constDeclList.getClass() ==  ConstDeclChar.class) {
				if(varType.getKind() != Struct.Char)
					report_error("Greska: Nekompatibilni tipovi u dodeli vrednosti konstanti ", constDeclList);
				ConstDeclChar constDeclChar = (ConstDeclChar)constDeclList;
				if(curNamespace.equals("")) {
					var_names.add(0, constDeclChar.getVarName());
				}else {
					var_names.add(0, curNamespace+"::"+ constDeclChar.getVarName());
				}
				var_values.add(0, constDeclChar.getChar2().charAt(1) + 0);
			}else if(constDeclList.getClass() ==  ConstDeclBool.class) {
				if(varType.getKind() != Struct.Bool)
					report_error("Greska: Nekompatibilni tipovi u dodeli vrednosti konstanti ", constDeclList);
				ConstDeclBool constDeclBool = (ConstDeclBool)constDeclList;
				if(curNamespace.equals("")) {
					var_names.add(0, constDeclBool.getVarName());
				}else {
					var_names.add(0, curNamespace+"::"+ constDeclBool.getVarName());
				}
				int boolValue = 0;
				if(constDeclBool.getBool().getClass() == BoolTrue.class) {
					boolValue=1;
				}else if(constDeclBool.getBool().getClass() == BoolFalse.class) {
					boolValue=0;
				}
				var_values.add(0, '0' + boolValue);
			}
		}
		
		for(int i=0; i<var_names.size(); i++) {
			varDeclCount++;
				report_info("Deklarisana konstanta "+ var_names.get(i), constDeclList);
				Obj varNode = Tab.insert(Obj.Con,var_names.get(i), varType);
				varNode.setAdr(var_values.get(i));
		}
		
	} 
	
	@Override
	public void visit(FactorExpr factorExpr) {
		factorExpr.struct = factorExpr.getExpr().struct;
	}
	
	
	@Override
	public void visit(FactorNew factorNew) {
		if(factorNew.getExpr().struct != Tab.intType)
			report_error("Greska u liniji "+factorNew.getLine()+": velicina niza mora biti tipa int", factorNew);
		factorNew.struct = new Struct(Struct.Array, factorNew.getType().struct);
	}
	
	
	@Override
	public void visit(NamespaceDeclarationName namespaceDeclarationName) {
		curNamespace = namespaceDeclarationName.getNamespaceName();
	}
	
	@Override
	public void visit(NamespaceDeclaration namespaceDeclaration) {
		curNamespace = "";
	}
	
	@Override
	public void visit(ReadStmt readStmt) {
		/*if(readStmt.getDesignator().obj.getType() != Tab.intType && readStmt.getDesignator().obj.getType() != Tab.charType) {
			report_error ("Semanticka greska na liniji " + readStmt.getLine() + ": Operand instrukcije READ mora biti char ili int tipa", null );
		}*/
	}
	
	
	@Override
	public void visit(Increment increment) {
		if(!(increment.getDesignator().obj.getKind() == Obj.Var && increment.getDesignator().obj.getType().getKind() == Struct.Int
				||
			increment.getDesignator().obj.getKind() == Obj.Elem && increment.getDesignator().obj.getType().getKind() == Struct.Int
		  )) 
		{
			report_error("Greska: neispran tip promenljive za ++", increment);
		}
	}
	
	
	@Override
	public void visit(FuncCall funcCall) {
		Obj o = funcCall.getDesignator().obj;
        if (o.getKind()!=Obj.Meth )
            report_error("Greska: Ocekivan metod", funcCall);
        if (o.getType()==Tab.noType )
            report_error("Greska: Procedura pozvana kao funkcija", funcCall);
        funcCall.struct=o.getType();	
	}
	
	@Override
	public void visit(ProcCall funcCall) {
		Obj o = funcCall.getDesignator().obj;
        if (o.getKind()!=Obj.Meth )
            report_error("Greska: Ocekivan metod", funcCall);
        if (o.getType()==Tab.noType )
            report_error("Greska: Procedura pozvana kao funkcija", funcCall);
        funcCall.struct=o.getType();	
	}
	
	@Override
	public void visit(ProcCallStatement procCallStatement) { 
		Obj o = procCallStatement.getDesignator().obj;
		if (o.getKind()!=Obj.Meth )
			report_error("Greska: Ocekivan metod", procCallStatement);
		
	}
	
	@Override
	public void visit(FuncCallStatement funcCallStatement) { 
		Obj o = funcCallStatement.getDesignator().obj;
		if (o.getKind()!=Obj.Meth )
			report_error("Greska: Ocekivan metod", funcCallStatement);
	}
	
	
	@Override
	public void visit(FormParamLIdent formParamLIdent) {
		String paramName = curNamespace.equals("") ? formParamLIdent.getParamName() : curNamespace+"::"+formParamLIdent.getParamName();
		Tab.insert(Obj.Var, paramName, formParamLIdent.getType().struct);
	}
	
	@Override
	public void visit(FormParIdent formParIdent) {
		String paramName = curNamespace.equals("") ? formParIdent.getParamName() : curNamespace+"::"+formParIdent.getParamName();
		Tab.insert(Obj.Var, paramName, formParIdent.getType().struct);
	}
	
	@Override
	public void visit(FormParArr formParArr) {
		String paramName = curNamespace.equals("") ? formParArr.getParamName() : curNamespace+"::"+formParArr.getParamName();
		Tab.insert(Obj.Var, paramName, new Struct(Struct.Array, formParArr.getType().struct));
	}
	
	@Override
	public void visit(FormParamLArr formParamLArr) {
		String paramName = curNamespace.equals("") ? formParamLArr.getParamName() : curNamespace+"::"+formParamLArr.getParamName();
		Tab.insert(Obj.Var, paramName, new Struct(Struct.Array, formParamLArr.getType().struct));
	}
	
	@Override
	public void visit(RelopEq relopEq) {
		relopEq.integer = Code.eq;
	}
	
	@Override
	public void visit(RelopNeq relopNeq) {
		relopNeq.integer = Code.ne;
	}
	
	@Override
	public void visit(RelopGR relopGR) {
		relopGR.integer = Code.gt;
	}
	
	@Override
	public void visit(RelopGREQ relopGREQ) {
		relopGREQ.integer=Code.ge;
	}
	
	@Override
	public void visit(RelopLE relopLE) {
		relopLE.integer=Code.lt;
	}
	
	@Override
	public void visit(RelopLEQ relopLEQ) {
		relopLEQ.integer=Code.le;
	}
	
	@Override
	public void visit(CondFactRel condFactRel) {
		condFactRel.integer = condFactRel.getRelop().integer;
	}
	
	@Override
	public void visit(CondFactBool condFactBool) {
		condFactBool.integer = -1;
	}
}

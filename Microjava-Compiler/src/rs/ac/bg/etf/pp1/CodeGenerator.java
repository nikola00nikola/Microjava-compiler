package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.AddopExpr;
import rs.ac.bg.etf.pp1.ast.AddopMinus;
import rs.ac.bg.etf.pp1.ast.AddopPlus;
import rs.ac.bg.etf.pp1.ast.ArrAsignment;
import rs.ac.bg.etf.pp1.ast.ArrayStart;
import rs.ac.bg.etf.pp1.ast.Assignment;
import rs.ac.bg.etf.pp1.ast.BoolFalse;
import rs.ac.bg.etf.pp1.ast.BoolTrue;
import rs.ac.bg.etf.pp1.ast.BreakStatement;
import rs.ac.bg.etf.pp1.ast.CondFactBool;
import rs.ac.bg.etf.pp1.ast.CondFactRel;
import rs.ac.bg.etf.pp1.ast.CondTerm;
import rs.ac.bg.etf.pp1.ast.CondTermFact;
import rs.ac.bg.etf.pp1.ast.CondTermList;
import rs.ac.bg.etf.pp1.ast.ConditionOr;
import rs.ac.bg.etf.pp1.ast.ConditionTerm;
import rs.ac.bg.etf.pp1.ast.ContinueStatement;
import rs.ac.bg.etf.pp1.ast.Decrement;
import rs.ac.bg.etf.pp1.ast.Designator;
import rs.ac.bg.etf.pp1.ast.DesignatorArr;
import rs.ac.bg.etf.pp1.ast.DesignatorList;
import rs.ac.bg.etf.pp1.ast.DesignatorListComma;
import rs.ac.bg.etf.pp1.ast.DesignatorListElem;
import rs.ac.bg.etf.pp1.ast.DesignatorNmspcArr;
import rs.ac.bg.etf.pp1.ast.DesignatorNmspcVar;
import rs.ac.bg.etf.pp1.ast.DesignatorVar;
import rs.ac.bg.etf.pp1.ast.ElseTerminal;
import rs.ac.bg.etf.pp1.ast.Expression;
import rs.ac.bg.etf.pp1.ast.Expressions;
import rs.ac.bg.etf.pp1.ast.FactorBool;
import rs.ac.bg.etf.pp1.ast.FactorChar;
import rs.ac.bg.etf.pp1.ast.FactorDesignator;
import rs.ac.bg.etf.pp1.ast.FactorNew;
import rs.ac.bg.etf.pp1.ast.FactorNumber;
import rs.ac.bg.etf.pp1.ast.ForDontLoadEnd;
import rs.ac.bg.etf.pp1.ast.ForDontLoadStart;
import rs.ac.bg.etf.pp1.ast.ForStatement;
import rs.ac.bg.etf.pp1.ast.ForTerminal;
import rs.ac.bg.etf.pp1.ast.ForTopAddress;
import rs.ac.bg.etf.pp1.ast.FuncCall;
import rs.ac.bg.etf.pp1.ast.FuncCallStatement;
import rs.ac.bg.etf.pp1.ast.IfElseStatement;
import rs.ac.bg.etf.pp1.ast.IfTerm;
import rs.ac.bg.etf.pp1.ast.Increment;
import rs.ac.bg.etf.pp1.ast.MethodDecl;
import rs.ac.bg.etf.pp1.ast.MethodTypeName;
import rs.ac.bg.etf.pp1.ast.MethodTypeNameType;
import rs.ac.bg.etf.pp1.ast.MethodTypeNameVoid;
import rs.ac.bg.etf.pp1.ast.MinusTermExpr;
import rs.ac.bg.etf.pp1.ast.MulopDiv;
import rs.ac.bg.etf.pp1.ast.MulopMod;
import rs.ac.bg.etf.pp1.ast.MulopMul;
import rs.ac.bg.etf.pp1.ast.PrintStmt;
import rs.ac.bg.etf.pp1.ast.ProcCall;
import rs.ac.bg.etf.pp1.ast.ProcCallStatement;
import rs.ac.bg.etf.pp1.ast.ReadStmt;
import rs.ac.bg.etf.pp1.ast.ReturnOnly;
import rs.ac.bg.etf.pp1.ast.ReturnStmt;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.ast.TermMulop;
import rs.ac.bg.etf.pp1.ast.UnmatchedIf;
import rs.ac.bg.etf.pp1.ast.UnmatchedIfList;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	private int mainPc;
	private List<List<Integer>> listAddr = new ArrayList<>();
	private List<List<Integer>> listIfAddr = new ArrayList<>();
	private List<Integer> listIfJMP = new ArrayList<>();
	
	
	private List<Integer> listAddrForTop = new ArrayList<>();
	private List<List<Integer>> listAddrFor = new ArrayList<>();
	private List<List<Integer>> listAddrForContinue = new ArrayList<>();
	private boolean loadFlag = true;
	
	
	public int getMainPc(){
		return mainPc;
	}
	
	public void visit(PrintStmt printStmt){
		if(printStmt.getExpr().struct == Tab.intType){
			Code.loadConst(5);
			Code.put(Code.print);
		}else{
			Code.loadConst(1);
			Code.put(Code.bprint);
		}
	}
	
	
	public void visit(FactorNumber factorNum){
		if(!loadFlag)
			return;
		Obj con = Tab.insert(Obj.Con, "$", factorNum.struct);
		con.setLevel(0);
		con.setAdr(factorNum.getNum1());
		
		Code.load(con);
	}
	
	public void visit(FactorChar factorChar){
		if(!loadFlag)
			return;
		Obj con = Tab.insert(Obj.Con, "$", factorChar.struct);
		con.setLevel(0);
		char c = factorChar.getChar1().charAt(1);
		con.setAdr(c);
		
		Code.load(con);
	}
	
	@Override
	public void visit(FactorBool factorBool) {
		if(!loadFlag)
			return;
		int value=0;
		if(factorBool.getBool().getClass() == BoolTrue.class) {
			value=1;
		}else if( factorBool.getBool().getClass() == BoolFalse.class) {
			value=0;
		}
		Obj con = Tab.insert(Obj.Con, "$", factorBool.struct);
		con.setLevel(0);
		
		con.setAdr(value+'0');
		Code.load(con);
	}
	
	
	public void visit(MethodTypeNameType methodTypeName){
		
		if("main".equalsIgnoreCase(methodTypeName.getMethName())){
			mainPc = Code.pc;
		}
		methodTypeName.obj.setAdr(Code.pc);
		// Collect arguments and local variables
		SyntaxNode methodNode = methodTypeName.getParent();
	
		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);

		
		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);
		
		
		System.out.println("Metoda "+methodTypeName.getMethName()+" ima "+varCnt.getCount()+" lokalnih varijabli");
		System.out.println("Metoda "+methodTypeName.getMethName()+" ima "+fpCnt.getCount()+" formalnih parametara");
		// Generate the entry
		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(fpCnt.getCount() + varCnt.getCount());
	
	}
	
	public void visit(MethodTypeNameVoid methodTypeName){
		
		if("main".equalsIgnoreCase(methodTypeName.getMethName())){
			mainPc = Code.pc;
		}
		methodTypeName.obj.setAdr(Code.pc);
		// Collect arguments and local variables
		SyntaxNode methodNode = methodTypeName.getParent();
	
		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);

		
		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);
		
		
		System.out.println("Metoda "+methodTypeName.getMethName()+" ima "+varCnt.getCount()+" lokalnih varijabli");
		System.out.println("Metoda "+methodTypeName.getMethName()+" ima "+fpCnt.getCount()+" formalnih parametara");
		// Generate the entry
		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(fpCnt.getCount() + varCnt.getCount());
	
	}
	
	@Override
	public void visit(MethodDecl methodDecl) {
		Code.put(Code.exit); 
		Code.put(Code.return_);
	}
	
	@Override
	public void visit(ReturnOnly returnOnly) {
		Code.put(Code.exit); 
		Code.put(Code.return_);
	}
	
	public void visit(Assignment assignment){
		if(!loadFlag)
			return;
		Code.store(assignment.getDesignator().obj);
	}
	
	public void visit(FactorDesignator factorDesignator){
		if(!loadFlag)
			return;
		if(factorDesignator.getDesignator().getClass() == DesignatorVar.class) {
			Code.load(factorDesignator.getDesignator().obj);
		}
		else if(factorDesignator.getDesignator().getClass() == DesignatorArr.class) {
			Code.load(factorDesignator.getDesignator().obj);
		}else if(factorDesignator.getDesignator().getClass() == DesignatorNmspcVar.class) {
			Code.load(factorDesignator.getDesignator().obj);
		}else if(factorDesignator.getDesignator().getClass() == DesignatorNmspcArr.class) {
			Code.load(factorDesignator.getDesignator().obj);
		}
		
	}
	
	public void visit(ReturnStmt returnStmt){
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	

	@Override
	public void visit(AddopExpr addopExpr) {
		if(!loadFlag)
			return;
		
		if(addopExpr.getAddop().getClass() == AddopPlus.class) {
			Code.put(Code.add);
		}else if(addopExpr.getAddop().getClass() == AddopMinus.class) {
			Code.put(Code.sub);
		}
	}
	
	
	@Override
	public void visit(TermMulop termMulop) {
		if(!loadFlag)
			return;
		if(termMulop.getMulop().getClass() == MulopMul.class) {
			Code.put(Code.mul);
		}else if (termMulop.getMulop().getClass() == MulopDiv.class) {
			Code.put(Code.div);
		}else if (termMulop.getMulop().getClass() == MulopMod.class) {
			Code.put(Code.rem);
		}
	}
	
	
	
	@Override
	public void visit(FactorNew factorNew) {
        Code.put(Code.newarray);
        if ( factorNew.getType().struct == Tab.charType || factorNew.getType().struct.getKind() == Struct.Bool ) 
			Code.put(0); 
        else if (factorNew.getType().struct == Tab.intType)
			Code.put(1);
	}
	
	
	@Override
	public void visit(ArrayStart arrayStart) {
		if(!loadFlag)
			return;
		Code.load(arrayStart.obj);
	}
	
	
	@Override
	public void visit(ReadStmt readStmt) {
		if(readStmt.getDesignator().obj.getType() == Tab.intType) {
			Code.put(Code.read);
			Code.store(readStmt.getDesignator().obj);
		}else if(readStmt.getDesignator().obj.getType() == Tab.charType) {
			Code.put(Code.bread);
			Code.store(readStmt.getDesignator().obj);
		}else if(readStmt.getDesignator().obj.getType() == Tab.find("bool").getType()) {
			Code.put(Code.bread);
			Code.store(readStmt.getDesignator().obj);
		}
	}
	
	@Override
	public void visit(MinusTermExpr minusTermExpr) {
		if(!loadFlag)
			return;
		Code.put(Code.neg);
	}
	
	@Override
	public void visit(Increment increment) {
		if(!loadFlag)
			return;
		if(increment.getDesignator().getClass() == DesignatorArr.class || increment.getDesignator().getClass() == DesignatorNmspcArr.class) {
			ArrayIncrementVisitor arrVisitor = new ArrayIncrementVisitor();
			increment.traverseBottomUp(arrVisitor);
		}
		
		Code.load(increment.getDesignator().obj);
		Obj con = Tab.insert(Obj.Con, "$", increment.getDesignator().obj.getType().getKind() != Struct.Array ? increment.getDesignator().obj.getType() : increment.getDesignator().obj.getType().getElemType());
		con.setLevel(0);
		con.setAdr(1);
		Code.load(con);
		Code.put(Code.add);
		Code.store(increment.getDesignator().obj);
	}
	
	@Override
	public void visit(Decrement decrement) {
		if(!loadFlag)
			return;
		if(decrement.getDesignator().getClass() == DesignatorArr.class || decrement.getDesignator().getClass() == DesignatorNmspcArr.class) {
			ArrayIncrementVisitor arrVisitor = new ArrayIncrementVisitor();
			decrement.traverseBottomUp(arrVisitor);
		}
		
		Code.load(decrement.getDesignator().obj);
		Obj con = Tab.insert(Obj.Con, "$", decrement.getDesignator().obj.getType().getKind() != Struct.Array ? decrement.getDesignator().obj.getType() : decrement.getDesignator().obj.getType().getElemType());
		con.setLevel(0);
		con.setAdr(1);
		Code.load(con);
		Code.put(Code.sub);
		Code.store(decrement.getDesignator().obj);
	}
	
	
	public void visit(ProcCallStatement procCallStatement) { 
		if(!loadFlag)
			return;
		Obj o = procCallStatement.getDesignator().obj;
		int dest_adr=o.getAdr()-Code.pc; // racunanje relativne adrese 
		Code.put(Code.call); 
		Code.put2(dest_adr);
		if (o.getType()!=Tab.noType )
			Code.put(Code.pop); // rezultat poziva nece biti koriscen
	}
	
	public void visit(FuncCallStatement funcCallStatement) { 
		if(!loadFlag)
			return;
		Obj o = funcCallStatement.getDesignator().obj;
		int dest_adr=o.getAdr()-Code.pc; // racunanje relativne adrese 
		Code.put(Code.call); 
		Code.put2(dest_adr);
		if (o.getType()!=Tab.noType )
			Code.put(Code.pop); // rezultat poziva nece biti koriscen
	}
	
	
    public void visit(Expressions expressions) {
		//Code.put(Code.pop); // stvarni parametri nisu implementirani, pa da ne ostanu na steku
	}

    public void visit(Expression expression) {
		//Code.put(Code.pop); // stvarni parametri nisu implementirani, pa da ne ostanu na steku
	}
    
    public void visit(FuncCall funcCall) {
		if(!loadFlag)
			return;
		// obrada stvarnih parametara nije implementirana
		Obj o = funcCall.getDesignator().obj;
        int dest_adr=o.getAdr()-Code.pc; // racunanje relativne adrese 
        Code.put(Code.call); 
        Code.put2(dest_adr);
        
	}
    
    
    public void visit(ProcCall procCall) {
		if(!loadFlag)
			return;
		// obrada stvarnih parametara nije implementirana
		Obj o = procCall.getDesignator().obj;
        int dest_adr=o.getAdr()-Code.pc; // racunanje relativne adrese 
        Code.put(Code.call); 
        Code.put2(dest_adr);
        
	}
    
    
    
    

    
   /* @Override
    public void visit(CondTermFact condTermFact) {
    	int op = condTermFact.getCondFact().integer;
    	if(op==-1) {
    		op=Code.eq;
    		Obj con = Tab.insert(Obj.Con, "$", new Struct(Struct.Bool));
			con.setLevel(0);
			con.setAdr(1 + '0');
			Code.load(con);
    	}
    	Code.putFalseJump(op, 0);
    	listAddr.get(listAddr.size()-1).add(Code.pc - 2);
    	
    }
    
    @Override
    public void visit(CondTermList condTermList) {
    	int op = condTermList.getCondFact().integer;
    	if(op==-1) {
    		op=Code.eq;
    		Obj con = Tab.insert(Obj.Con, "$", new Struct(Struct.Bool));
			con.setLevel(0);
			con.setAdr(1 + '0');
			Code.load(con);
    	}
    	Code.putFalseJump(op, 0);
    	listAddr.get(listAddr.size()-1).add(Code.pc - 2);
    }*/
    
    @Override
    public void visit(IfTerm ifTerm) {
    	listAddr.add(new ArrayList<>());
    	listIfAddr.add(new ArrayList<>());
    }
    
    @Override
    public void visit(ConditionOr conditionOr) {
    	if(conditionOr.getParent().getClass() == ConditionOr.class) {
    		//nije poslednji CondTerm
    		Code.putJump(0);
    		listIfAddr.get(listIfAddr.size()-1).add(Code.pc-2);
    		for(int addr : listAddr.get(listAddr.size()-1)) {
    			Code.fixup(addr);
    		}
    		listAddr.get(listAddr.size()-1).clear();
    		
    	}else {
    		//poslednji condTerm
    		
    		for(int addr : listIfAddr.get(listIfAddr.size()-1)) {
    			Code.fixup(addr);
    		}
    		listIfAddr.remove(listIfAddr.size()-1);
    		
    		//nema JMP jer sledi telo IF-a
    	}
    }
    
    @Override
    public void visit(ConditionTerm conditionTerm) {
    	if(conditionTerm.getParent().getClass() == ConditionOr.class) {
    		//nije poslednji CondTerm
    		Code.putJump(0);
    		listIfAddr.get(listIfAddr.size()-1).add(Code.pc-2);
    		for(int addr : listAddr.get(listAddr.size()-1)) {
    			Code.fixup(addr);
    		}
    		listAddr.get(listAddr.size()-1).clear();
    	}else {
    		//poslednji condTerm
    		
    		for(int addr : listIfAddr.get(listIfAddr.size()-1)) {
    			Code.fixup(addr);
    		}
    		listIfAddr.remove(listIfAddr.size()-1);
    		
    		//nema JMP jer sledi telo IF-a
    	}
    }
 
    
    @Override
    public void visit(ElseTerminal elseTerminal) {
    	
    	Code.putJump(0);
    	listIfJMP.add(Code.pc-2);
    	
    	for(int addr : listAddr.get(listAddr.size()-1)){
    		Code.fixup(addr);
    	}
    	listAddr.remove(listAddr.size()-1);
    }
    
    @Override
    public void visit(UnmatchedIf unmatchedIf) {
	    for(int addr : listAddr.get(listAddr.size()-1)){
	    	Code.fixup(addr);
	    }
	    listAddr.remove(listAddr.size()-1);
    }
    
    @Override
    public void visit(UnmatchedIfList unmatchedIfList) {
    	Code.fixup(listIfJMP.get(listIfJMP.size()-1));
    	listIfJMP.remove(listIfJMP.size()-1);
    }
    
    @Override
    public void visit(IfElseStatement ifElseStatement) {
    	Code.fixup(listIfJMP.get(listIfJMP.size()-1));
    	listIfJMP.remove(listIfJMP.size()-1);
    }
    
    @Override
    public void visit(ForDontLoadStart forDontLoadStart) {
    	loadFlag=false;
    }
    
    @Override
    public void visit(ForTopAddress orTopAddress) {
    	listAddrFor.add(new ArrayList<>());
    	listAddrForContinue.add(new ArrayList<>());
    	listAddrForTop.add(Code.pc);
    }
    
    @Override
    public void visit(ForDontLoadEnd forDontLoadEnd) {
    	loadFlag=true;
    }
    
    @Override
    public void visit(BreakStatement breakStatement) {
    	Code.putJump(0);
    	listAddrFor.get(listAddrFor.size()-1).add(Code.pc - 2);
    }
   
    @Override
    public void visit(ContinueStatement continueStatement) {
    	Code.putJump(0);
    	listAddrForContinue.get(listAddrForContinue.size()-1).add(Code.pc - 2);
    }
    
    @Override
    public void visit(CondFactRel condFactRel) {
    	int op = condFactRel.integer;
    	
    	Code.putFalseJump(op, 0);
    	
    	if(condFactRel.getParent().getClass() == ForDontLoadStart.class ) {
    		listAddrFor.get(listAddrFor.size()-1).add(Code.pc-2);
    	}else if(condFactRel.getParent() instanceof CondTerm) {
    		listAddr.get(listAddr.size()-1).add(Code.pc-2);
    	}
    }
    
    
    @Override
    public void visit(CondFactBool condFactBool) {
		Obj con = Tab.insert(Obj.Con, "$", new Struct(Struct.Bool));
		con.setLevel(0);
		con.setAdr(1 + '0');
		Code.load(con);
		int op=Code.eq;
		
    	if(condFactBool.getParent().getClass() == ForDontLoadStart.class ) {
    		Code.putFalseJump(op, 0);
    		listAddrFor.get(listAddrFor.size()-1).add(Code.pc-2);
    	}else if(condFactBool.getParent() instanceof CondTerm) {
    		Code.putFalseJump(op, 0);
    		listAddr.get(listAddr.size()-1).add(Code.pc-2);
    	}
    }
    
    @Override
    public void visit(ForStatement forStatement) {
    	for(int addr : listAddrForContinue.get(listAddrForContinue.size()-1)) {
    		Code.fixup(addr);
    	}
    	
    	CodeGenerator cg = new CodeGenerator();
    	forStatement.getForDontLoadEnd().getDesignatorStatementFor().traverseBottomUp(cg);
    	
    	Code.putJump(listAddrForTop.get(listAddrForTop.size()-1));
    	listAddrForTop.remove(listAddrForTop.size()-1);
    	for(int addr : listAddrFor.get(listAddrFor.size()-1)) {
    		Code.fixup(addr);
    	}
    	listAddrFor.remove(listAddrFor.size()-1);
    	listAddrForContinue.remove(listAddrForContinue.size()-1);
    }
    
    
    
    
    
    int duzinaListe=0;
    
    @Override
    public void visit(DesignatorListComma designatorListComma) {
    	duzinaListe++;
    }
    
    @Override
    public void visit(DesignatorListElem designatorListElem) {
    	duzinaListe++;
    }
    
    
    @Override
    public void visit(ArrAsignment arrAsignment) {
    	Code.load(arrAsignment.getDesignator().obj);
    	Code.put(Code.arraylength);
    	Code.loadConst(duzinaListe);
    	Code.put(Code.add);
    	
    	Code.load(arrAsignment.getDesignator1().obj);
    	Code.put(Code.arraylength);
    	
    	Code.putFalseJump(Code.ne, 0);
    	int addrGood = Code.pc-2;
    	Code.put(Code.trap);
    	Code.fixup(addrGood);
    	
    	
    	Obj dummy = new Obj(Obj.Elem, "", arrAsignment.getDesignator().obj.getType());
    	Obj i1 = Tab.insert(Obj.Var, "i1_", Tab.intType);
    	//i1.setLevel(0);
    	Obj i2 = Tab.insert(Obj.Var, "i2_", Tab.intType);
    	//i2.setLevel(0);
    	
    	Code.load(arrAsignment.getDesignator().obj);
    	Code.put(Code.arraylength);
    	Code.loadConst(1);
    	Code.put(Code.sub);
    	Code.store(i1);
    	
    	Code.load(arrAsignment.getDesignator1().obj);
    	Code.put(Code.arraylength);
    	Code.loadConst(1);
    	Code.put(Code.sub);
    	Code.store(i2);
    	
    	
    	int loopStart = Code.pc;
    	Code.load(i1);
    	Code.loadConst(0);
    	Code.putFalseJump(Code.ge, 0);
    	int addrSkip = Code.pc-2;
    	Code.load(arrAsignment.getDesignator().obj);
    	Code.load(i1);
    	
    	Code.load(arrAsignment.getDesignator1().obj);
    	Code.load(i2);
    	Code.load(dummy);
    	
    	Code.store(dummy);
    	
    	Code.load(i1);
    	Code.loadConst(1);
    	Code.put(Code.sub);
    	Code.store(i1);
    	
    	Code.load(i2);
    	Code.loadConst(1);
    	Code.put(Code.sub);
    	Code.store(i2);
    	
    	Code.putJump(loopStart);
    	
    	Code.fixup(addrSkip);
    	
    	
    	
    	
    	
    	
		
		DesignatorList designatorList = arrAsignment.getDesignatorList();
		
		while(designatorList instanceof DesignatorListElem || designatorList instanceof DesignatorListComma) {
			
			if(designatorList instanceof DesignatorListElem) {
				Code.load(arrAsignment.getDesignator1().obj);
				Code.load(i2);
				Code.load(dummy);
				DesignatorListElem designatorListElem = (DesignatorListElem)designatorList;
				Code.store(designatorListElem.getDesignator().obj);
				designatorList = (designatorListElem).getDesignatorList();
			}else if(designatorList instanceof DesignatorListComma) {
				DesignatorListComma designatorListComma=(DesignatorListComma)designatorList;
				
				designatorList = (designatorListComma).getDesignatorList();
				
			}
			
			
			Code.load(i2);
			Code.loadConst(1);
			Code.put(Code.sub);
			Code.store(i2);
		}
    	
    	duzinaListe=0;
    }
	
/*
	
	public void visit(Const cnst){
		Obj con = Tab.insert(Obj.Con, "$", cnst.struct);
		con.setLevel(0);
		con.setAdr(cnst.getN1());
		
		Code.load(con);
	}
	
	public void visit(MethodTypeName methodTypeName){
		
		if("main".equalsIgnoreCase(methodTypeName.getMethName())){
			mainPc = Code.pc;
		}
		methodTypeName.obj.setAdr(Code.pc);
		// Collect arguments and local variables
		SyntaxNode methodNode = methodTypeName.getParent();
	
		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);
		
		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);
		
		// Generate the entry
		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(fpCnt.getCount() + varCnt.getCount());
	
	}
	
	public void visit(MethodDecl methodDecl){
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(Assignment assignment){
		Code.store(assignment.getDesignator().obj);
	}
	
	public void visit(Designator designator){
		SyntaxNode parent = designator.getParent();
		
		if(Assignment.class != parent.getClass() && FuncCall.class != parent.getClass() && ProcCall.class != parent.getClass()){
			Code.load(designator.obj);
		}
	}
	
	public void visit(FuncCall funcCall){
		Obj functionObj = funcCall.getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);
		
		Code.put2(offset);
	}
	
	public void visit(ProcCall procCall){
		Obj functionObj = procCall.getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		if(procCall.getDesignator().obj.getType() != Tab.noType){
			Code.put(Code.pop);
		}
	}
	
	public void visit(ReturnExpr returnExpr){
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(ReturnNoExpr returnNoExpr){
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(AddExpr addExpr){
		Code.put(Code.add);
	}*/

}

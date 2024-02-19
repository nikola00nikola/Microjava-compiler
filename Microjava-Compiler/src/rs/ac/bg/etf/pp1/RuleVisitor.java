package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class RuleVisitor extends VisitorAdaptor{
	
	
	
	public void visit(FactorNumber factorNum){
		Obj con = Tab.insert(Obj.Con, "$", factorNum.struct);
		con.setLevel(0);
		con.setAdr(factorNum.getNum1());
		
		Code.load(con);
	}
	
	public void visit(FactorChar factorChar){
		Obj con = Tab.insert(Obj.Con, "$", factorChar.struct);
		con.setLevel(0);
		char c = factorChar.getChar1().charAt(1);
		con.setAdr(c);
		
		Code.load(con);
	}
	
	@Override
	public void visit(FactorBool factorBool) {
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
	
	
	public void visit(Assignment assignment){
		Code.store(assignment.getDesignator().obj);
	}
	
	public void visit(FactorDesignator factorDesignator){
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

	@Override
	public void visit(AddopExpr addopExpr) {
		
		if(addopExpr.getAddop().getClass() == AddopPlus.class) {
			Code.put(Code.add);
		}else if(addopExpr.getAddop().getClass() == AddopMinus.class) {
			Code.put(Code.sub);
		}
	}
	
	
	@Override
	public void visit(TermMulop termMulop) {
		if(termMulop.getMulop().getClass() == MulopMul.class) {
			Code.put(Code.mul);
		}else if (termMulop.getMulop().getClass() == MulopDiv.class) {
			Code.put(Code.div);
		}else if (termMulop.getMulop().getClass() == MulopMod.class) {
			Code.put(Code.rem);
		}
	}
	
	
	
	@Override
	public void visit(ArrayStart arrayStart) {
		Code.load(arrayStart.obj);
	}
	

	
	@Override
	public void visit(MinusTermExpr minusTermExpr) {
		Code.put(Code.neg);
	}
	
	@Override
	public void visit(Increment increment) {
		if(increment.getDesignator().getClass() == DesignatorArr.class) {
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
		if(decrement.getDesignator().getClass() == DesignatorArr.class) {
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
		Obj o = procCallStatement.getDesignator().obj;
		int dest_adr=o.getAdr()-Code.pc; // racunanje relativne adrese 
		Code.put(Code.call); 
		Code.put2(dest_adr);
		if (o.getType()!=Tab.noType )
			Code.put(Code.pop); // rezultat poziva nece biti koriscen
	}
	
	public void visit(FuncCallStatement funcCallStatement) { 
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
		// obrada stvarnih parametara nije implementirana
		Obj o = funcCall.getDesignator().obj;
        int dest_adr=o.getAdr()-Code.pc; // racunanje relativne adrese 
        Code.put(Code.call); 
        Code.put2(dest_adr);
        
	}
    
    
    public void visit(ProcCall procCall) {
		// obrada stvarnih parametara nije implementirana
		Obj o = procCall.getDesignator().obj;
        int dest_adr=o.getAdr()-Code.pc; // racunanje relativne adrese 
        Code.put(Code.call); 
        Code.put2(dest_adr);
        
	}
    
    
    
    
    


}

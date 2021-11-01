package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {

	private int mainPc;
	private Struct boolType = Tab.find("bool").getType();
	
	public List<Integer> listaAdresa = new ArrayList<Integer>();
	
	public int adr_samo_if = 0;
	
	public int getMainPc() { 
		return mainPc;
	}
	
	public void visit(PrintStmt print) { 
		if(print.getExpression().struct == Tab.intType) { 
			Code.loadConst(5);
			Code.put(Code.print);
		} else if (print.getExpression().struct == boolType) { 
			Code.loadConst(1);
			Code.put(Code.print);
		} else { 
			Code.loadConst(1); 
			Code.put(Code.bprint);
		}
	}
	
	public void visit(PrintStmtFormat psf) { 
		int format = psf.getN2();
		if(psf.getExpression().struct == Tab.intType) { 
			Code.loadConst(format);
			Code.put(Code.print);
		} else if (psf.getExpression().struct == boolType) { 
			Code.loadConst(format);
			Code.put(Code.print);
		} else { 
			Code.loadConst(format); 
			Code.put(Code.bprint);
		}
	}
	
	public void visit(ReadStmt readStmt) { 
		if(readStmt.getDesignator().obj.getType().equals(Tab.charType)) { 
			Code.put(Code.bread);
		}else { Code.put(Code.read); }
		
		Code.store(readStmt.getDesignator().obj);
	}
	//FACTOR-i
	public void visit(FactVar var) {
		Code.load(var.getDesignator().obj);
	}
	
	public void visit(FactConst fConst) { 
		Code.loadConst(fConst.getVr());
	}
	public void visit(ConstChar fChar) { 
		Code.loadConst(fChar.getC1());
	}
	public void visit(ConstBool fBool) { 
		if(fBool.getB().booleanValue() == true) Code.loadConst(1);
			else Code.loadConst(0);
	}
	public void visit(Allocation fAlloc) {
		Code.put(Code.newarray); 
		if(fAlloc.struct.getElemType().equals(Tab.charType))  { 
			Code.put(0);
		} else { 
			Code.put(1);
		}
	}
	
	//DESIGNATORI ++ i --
	public void visit(PlusPlusDesignator desigpp) { 
		Code.load(desigpp.getDesignator().obj);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(desigpp.getDesignator().obj);
	}
	
	public void visit(MinusMinusDesignator desigpp) { 
		Code.load(desigpp.getDesignator().obj);
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(desigpp.getDesignator().obj);
	}
	

	public void visit(DesignatorIdent designIdent) {  //INDEKSIRANJE NIZA
		Code.load(designIdent.obj);		
	}
	
	
	public void visit(AssignDesignator assn) { 
		Code.store(assn.getDesignator().obj);
	}
	
	//Expression MINUS
	public void visit(Expression expr) { 
		if(expr.getMinus().obj != null) { 
			Code.put(Code.neg);
		}
	}

	public void visit(MethodTypeName mTypeName) { 
		mTypeName.obj.setAdr(Code.pc);
		if(mTypeName.obj.getName().equals("main")) { 
			mainPc=Code.pc;
		}
		
		//Argumenti i lokalne prom
		SyntaxNode methodNode = mTypeName.getParent();
		
		VarCounter varCnt = new VarCounter(); 
		methodNode.traverseTopDown(varCnt);
		
		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);
		
		// Generisanje enter instr
		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(fpCnt.getCount() + varCnt.getCount());	
	}
	
	public void visit(MethodDecl methodDecl) { 
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	


	/*  
	 //Pozivi funkcija u izrazima. Prvo u expression drugo u Statement List.
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
	 */
	
	public void visit(AddExpression addExpr) { 
		if (addExpr.getAddOp().obj.getFpPos() == Code.add) Code.put(Code.add);
		else Code.put(Code.sub); 
	}
	
	public void visit(TermMul mulExpr) { 
		switch(mulExpr.getMulOp().obj.getFpPos()) { 
			case Code.mul: {Code.put(Code.mul); break;}
			case Code.div: {Code.put(Code.div); break;}
			case Code.rem: {Code.put(Code.rem); break;}
		}
	}
	
	
	//CONDITIONI I IF!!!!!!!
	/*
	 * public void visit(CondFactDouble cf) {
	 * Code.putFalseJump(cf.getRelOp().obj.getFpPos(), 0);
	 * 
	 * 
	 * }
	 */
	
	
	public void visit(CondFactSingle cond) { 
		 Code.loadConst(0);
		 Code.putFalseJump(Code.gt, 0);
		 adr_samo_if = Code.pc - 2;
		 //listaAdresa.add(Code.pc - 2);	 
	}
	
	public void visit(CondFactDouble cond) { 
		 Code.putFalseJump(cond.getRelOp().obj.getFpPos(), 0);
		 adr_samo_if = Code.pc - 2;
		 //listaAdresa.add(Code.pc - 2);
	}
	
	public void visit(IfStmt ifstm) { 
		Code.fixup(adr_samo_if);
	}
	
	public void visit(IfElseStmt ifElseStm) { 
		Code.fixup(adr_samo_if);
	}

	public void visit(ElseNonTerminal elsenonTerm) { 
		Code.putJump(0);
		Code.fixup(adr_samo_if);
		adr_samo_if = Code.pc - 2;
	}
	
	public void visit(ConditionTermList ct) {     // AKO JEE AND OPERATOR! Na kontra uslov SKACEM ili na ELSE ili na kraj 
		
	}
}

package rs.ac.bg.etf.pp1;

import java.util.List;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.test.CompilerError;
import rs.ac.bg.etf.pp1.test.CompilerError.CompilerErrorType;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor {
		
	boolean errorDetected = false;
	Logger log = Logger.getLogger(getClass());
	List<CompilerError> list;
	CompilerErrorType tipGreske = CompilerErrorType.SEMANTIC_ERROR;
	
	public SemanticAnalyzer(List<CompilerError> l) { 
		list = l;
	}
	
	/*public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}
	*/
	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}
	
	  public int nVars = 0;
	  
	  private Struct lastConstType = null;
	  private Struct lastVarType = null ;
	  public Obj currentMethod = null;   //cija def se trenutno obradjuje. Postavljam je kad otvorim scope, kad zatvorim ulancam je u locals
	  
	  
	  Struct boolType = Tab.find("bool").getType(); 
	  Struct intType = Tab.find("int").getType(); 
	  Struct charType = Tab.find("char").getType();	  
	  
	  // ********* VAR DECLARATION *****************//
	  public void  visit(VarDeclaration varDecl) { 
			Obj res = Tab.currentScope.findSymbol(varDecl.getVarName());
			if(res != null) { 
				//report_error("Redefinicija simbola " + varDecl.getVarName(), varDecl);
				list.add(new CompilerError(varDecl.getLine(), "Redefinicija simbola " + varDecl.getVarName(), tipGreske));
			} else { 
				Tab.insert(Obj.Var, varDecl.getVarName(), lastVarType ); 
				// varDecls++;
			}			    
	   } 
	  public void  visit(VarDeclarationSquare varDecl) { 
			Obj res = Tab.currentScope.findSymbol(varDecl.getVarName());
			if(res != null) { 
				//report_error("Redefinicija simbola " + varDecl.getVarName(), varDecl);
				list.add(new CompilerError(varDecl.getLine(), "Redefinicija simbola " + varDecl.getVarName(), tipGreske));
			} else { 
				Tab.insert(Obj.Var, varDecl.getVarName(), new Struct(Struct.Array, lastVarType)); 
			}	
	  }
	  public void  visit(VarTypeDeclaration varDecl) { 
		  lastVarType = varDecl.getType().struct;
			Obj res = Tab.currentScope.findSymbol(varDecl.getVarName());
			if(res != null) { 
				//report_error("Redefinicija simbola " + varDecl.getVarName(), varDecl);
				list.add(new CompilerError(varDecl.getLine(), "Redefinicija simbola " + varDecl.getVarName(), tipGreske));
			} else { 
				Tab.insert(Obj.Var, varDecl.getVarName(), lastVarType ); 
			}
	  }  
	  public void  visit(VarTypeDeclarationSquare varDecl) {
		  lastVarType = varDecl.getType().struct;
			Obj res = Tab.currentScope.findSymbol(varDecl.getVarName());
			if(res != null) { 
				//report_error("Redefinicija simbola " + varDecl.getVarName(), varDecl);
				list.add(new CompilerError(varDecl.getLine(), "Redefinicija simbola " + varDecl.getVarName(), tipGreske));
			} else { 
				Tab.insert(Obj.Var, varDecl.getVarName(), new Struct(Struct.Array, lastVarType)); 
			}
	  }
	  
	  
	  // **** PROGRAM ********//
	   public void visit(ProgName ProgName) { 
		   ProgName.obj = Tab.insert(Obj.Prog, ProgName.getPName(), Tab.noType);
		   Tab.openScope();
	   }  
	   public void visit(ProgramNoPars pnp) { 
		   nVars = Tab.currentScope().getnVars();
		   Tab.chainLocalSymbols(pnp.getProgName().obj);
		   Tab.closeScope();
	   }
	   public void visit(ProgramPars pp) { 
		   nVars = Tab.currentScope().getnVars();
		   Tab.chainLocalSymbols(pp.getProgName().obj);
		   Tab.closeScope();
	   }
	  
	   // **** TYPE ******//
	    public void visit(Type type){
	    	Obj typeNode = Tab.find(type.getTypeName());
	    	if(typeNode == Tab.noObj){
	    		// report_error("Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola! ", null);
	    		list.add(new CompilerError(type.getLine(),"Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola! ", tipGreske));
	    		type.struct = Tab.noType;
	    	}else{
	    		if(Obj.Type == typeNode.getKind()){
	    			type.struct = typeNode.getType();
	    		}else{
	    			//report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip!", type);
	    			list.add(new CompilerError(type.getLine(), "Greska: Ime " + type.getTypeName() + " ne predstavlja tip!", tipGreske));
	    			type.struct = Tab.noType;
	    		}
	    	}
	    }
	    
	  
	//PREPISIVANJE TIPA
	public void visit(FType ftype) { 
		ftype.struct = ftype.getType().struct;
	}
	public void visit(FVoid fvoid) { 
		fvoid.struct = Tab.noType;
	}
	
	
	//Main metoda 
	public void visit(MethodTypeName methodTypeName) { 
		if(Tab.currentScope().findSymbol(methodTypeName.getFuncName()) != null) {
			methodTypeName.obj = Tab.noObj;
			//report_error("Redefinicija identifikatora", methodTypeName);
			list.add(new CompilerError(methodTypeName.getLine(), "Redefinicija identifikatora" + methodTypeName.getFuncName(), tipGreske));
		}else { 
			currentMethod = Tab.insert(Obj.Meth, methodTypeName.getFuncName(), methodTypeName.getFuncType().struct);
			if(!currentMethod.getType().equals(Tab.noType)) {
				//report_error("Funkcija main mora biti void!", methodTypeName);
				list.add(new CompilerError(methodTypeName.getLine(), "Funkcija main mora biti void!", tipGreske));
			}
		}
		methodTypeName.obj = currentMethod;
		Tab.openScope();

	}
	public void visit(MethodDecl methodDecl) { 
		if(!currentMethod.getName().equals("main")) { 
			  // report_error("Funkcija main mora biti definisana!", methodDecl);
			   list.add(new CompilerError(methodDecl.getLine(), "Funkcija main mora biti definisana!", tipGreske));
		 }
		 Tab.chainLocalSymbols(currentMethod);
		 Tab.closeScope();
		 currentMethod = null;	
	}
	public void visit(FormalPars fp) { 
		//report_error("Funkcija main ne sme imati formalne paramtere!", fp);
		 list.add(new CompilerError(fp.getLine(), "Funkcija main ne sme imati formalne paramtere!", tipGreske));
	}
	
	
	// ***** Design, Term, Factor, Expression .... ******** //	
	//Statement
	public void visit(PrintStmt ps) { 
		int tip =ps.getExpression().struct.getKind();
		if(tip != Struct.Int && tip != Struct.Char && tip != Struct.Bool ) { 
			//report_error("Operand naredbe PRINT mora biti tipa INT, CHAR ili BOOL!", ps);
			 list.add(new CompilerError(ps.getLine(), "Operand naredbe PRINT mora biti tipa INT, CHAR ili BOOL!", tipGreske));
		}
		//report_info("Naredba PRINT", ps);
	}
	public void visit(ReadStmt rs) { 
		Obj designObj = rs.getDesignator().obj;
		if(designObj.getKind() != Obj.Var && designObj.getKind() != Obj.Elem ) {                     ///*designObj.getFpPos() != 1*/ umesto drugog dela &&
			//report_error("Operand naredbe READ mora biti promenljiva ili element niza!", rs);
			list.add(new CompilerError(rs.getLine(), "Operand naredbe READ mora biti promenljiva ili element niza!", tipGreske));
		}
		int tip = designObj.getType().getKind();
		if(tip != Struct.Int && tip != Struct.Char && tip != Struct.Bool ) { 
			//report_error("Operand naredbe READ mora biti tipa INT, CHAR ili BOOL!", rs);
			list.add(new CompilerError(rs.getLine(), "Operand naredbe READ mora biti tipa INT, CHAR ili BOOL!", tipGreske));
		}
		//report_info("Naredba READ", rs);
	}
	
	//DesignatorStatement
	public void visit(AssignDesignator assign) { 
		if(!assign.getExpression().struct.assignableTo(assign.getDesignator().obj.getType())) {
			//report_error("Tipovi nisu kompatibilni u naredbi dodele! ", assign);
			list.add(new CompilerError(assign.getLine(), "Tipovi nisu kompatibilni u naredbi dodele! ", tipGreske));
		}
		report_info("Upotrebljena promenljiva: " + assign.getDesignator().obj.getName() + " u naredbi dodele.", assign);
	}
	public void visit(PlusPlusDesignator desigpp) { 
		Obj designObj = desigpp.getDesignator().obj;
		if(designObj.getKind() != Obj.Var && designObj.getKind() != Obj.Elem) { 
			//report_error("Operand za operator ++ mora biti promenljiva ili element niza!", desigpp);
			list.add(new CompilerError(desigpp.getLine(), "Operand za operator ++ mora biti promenljiva ili element niza!", tipGreske));
		}
		if(designObj.getType().getKind() != Struct.Int) { 
			//report_error("Operand za operator ++ mora biti tipa INT!", desigpp);
			list.add(new CompilerError(desigpp.getLine(), "Operand za operator ++ mora biti tipa INT!", tipGreske));
		}
		//report_info("Primenjen operator ++", desigpp);
	}
	public void visit(MinusMinusDesignator desigpp) { 
		Obj designObj = desigpp.getDesignator().obj;
		if(designObj.getKind() != Obj.Var && designObj.getKind() != Obj.Elem) { 
			//report_error("Operand za operator -- mora biti promenljiva ili element niza!", desigpp);
			list.add(new CompilerError(desigpp.getLine(), "Operand za operator -- mora biti promenljiva ili element niza!", tipGreske));
		}
		if(designObj.getType().getKind() != Struct.Int) { 
			//report_error("Operand za operator -- mora biti tipa INT!", desigpp);
			list.add(new CompilerError(desigpp.getLine(), "Operand za operator -- mora biti tipa INT!", tipGreske));
		}
		//report_info("Primenjen operator --", desigpp);
	}
	
	//Designator 
	public void visit(VarDesignator varDesign) { 
		varDesign.obj = Tab.find(varDesign.getVarName());
		if(varDesign.obj == Tab.noObj) { 
			//report_error("Promenljiva " + varDesign.getVarName() + " nije deklarisana!", varDesign);
			list.add(new CompilerError(varDesign.getLine(), "Promenljiva " + varDesign.getVarName() + " nije deklarisana!", tipGreske));
		}
	}
	public void visit(ArrayDesignator arrDesig) {
		Obj var = Tab.find(arrDesig.getDesignatorIdent().obj.getName());
		if(var==Tab.noObj) { 
			//report_error("Identifikator " + arrDesig.getVarName() + "nije deklarisan! ", arrDesig);
			list.add(new CompilerError(arrDesig.getLine(), "Identifikator " + arrDesig.getDesignatorIdent().obj.getName() + "nije deklarisan! ", tipGreske));
		} else { 
			if(var.getType().getKind() != Struct.Array) { 
				//report_error("Identifikator " + arrDesig.getVarName() + " mora biti niz!", arrDesig);
				list.add(new CompilerError(arrDesig.getLine(), "Identifikator " + arrDesig.getDesignatorIdent().obj.getName() + " mora biti niz!", tipGreske));
			}else if(arrDesig.getExpression().struct.getKind() != Struct.Int) { 
				//report_error("Izraz za indeksiranje mora biti tipa int!", arrDesig);
				list.add(new CompilerError(arrDesig.getLine(), "Izraz za indeksiranje mora biti tipa int!", tipGreske));
			} else { 
				report_info("Upotreba simbola:" + var.getName(), arrDesig); 
			}
		}
		arrDesig.obj = new Obj(Obj.Elem, "", var.getType().getElemType());     //UMESTO VAR.GETNAME() MOZE PRAZNAN STRING
		// arrDesig.obj.setFpPos(1);              //ZNACI DA JE ELEMENT NIZA UMESTO DA KORISTIM kind: ELEM !!!!!!!!!!!!!!! 
	}
	
	
	
	//DESIGNATOR IDENT - NAKNADANO !!!!!!!!!!!!!!!!
    public void visit(DesignatorIdent desgIdent) { 
    	desgIdent.obj = Tab.find(desgIdent.getVarName());
    }
	//FACTOR
	public void visit(FactVar factVar) { 
		factVar.struct = factVar.getDesignator().obj.getType();
		report_info("Upotrebljena promenljiva: " + factVar.getDesignator().obj.getName() + " u izrazu(Expression).", factVar);
	}
	public void visit(FactConst factConst) { 
		factConst.struct = intType;
	}	
	public void visit(ConstChar constChar) { 
		constChar.struct = charType;
	}	
	public void visit(ConstBool cBool) { 
		cBool.struct = boolType;
	}	
	public void visit(FactExpression fExpr) { 
		fExpr.struct = fExpr.getExpression().struct;
	}	
	public void visit(Allocation alloc) { 
		if( alloc.getExpression().struct.equals(intType))
			alloc.struct =new Struct(Struct.Array, alloc.getType().struct);
		else { 
			alloc.struct = Tab.noType;
			//report_error("Expression mora biti tipa int pri alokaciji niza!", alloc);
			list.add(new CompilerError(alloc.getLine(), "Expression mora biti tipa int pri alokaciji niza!", tipGreske));
		}		
	}		
	
	
	//TERM
	public void visit(TermFactor termFact) { 
		termFact.struct = termFact.getFactor().struct; 
	}
	public void visit(TermMul termMul) { 
		if(termMul.getFactor().struct.equals(intType) && (termMul.getTerm().struct.equals(intType)))
			termMul.struct = intType;
		else { 
			termMul.struct = Tab.noType;
			//report_error("Samo int-ovi mogu da se mnoze ili dele!", termMul);
			list.add(new CompilerError(termMul.getLine(), "Samo int-ovi mogu da se mnoze/dele!", tipGreske));
		}
	}
	
	
	//ExpressionList
	public void visit(TermExpression termExpr) { 
		termExpr.struct = termExpr.getTerm().struct; 
	}
	public void visit(AddExpression addExpr) { 
		if(addExpr.getTerm().struct.equals(intType) && (addExpr.getExpressionList().struct.equals(intType)))
			addExpr.struct = intType;   
		else { 
			addExpr.struct = Tab.noType;
			//report_error("Samo int-ovi mogu da se sabiraju", addExpr);
			list.add(new CompilerError(addExpr.getLine(), "Samo int-ovi mogu da se sabiraju/oduzimaju", tipGreske));
		}
	}
	
	
	//Expression
	public void visit (Expression expr) { 
		if(expr.getMinus().obj != null) {           //znaci da je prepoznat - ispred Expression-a
			if(expr.getExpressionList().struct.equals(intType))
				expr.struct = expr.getExpressionList().struct;
			else { 		
				expr.struct = Tab.noType;
				//report_error("Samo tip int moze imati predznak minus", expr);
				list.add(new CompilerError(expr.getLine(), "Samo tip int moze imati predznak minus", tipGreske));
			}
		} else expr.struct = expr.getExpressionList().struct;		
	}
	
	//Minus
	public void visit(Negative n) { 
		n.obj = new Obj(1, "name", Tab.noType);  //bitno da nije null!
	}
	
	public void visit(Positive p) { 
		p.obj = null;
	}
	

	// ********* CONST */*****************
	
	public void visit(ConstantBool constBool) { 
		constBool.obj = new Obj(Obj.Con, "", boolType, constBool.getB() ? 1 : 0, 0);  	
	}	
	public void visit(ConstantChar constChar) { 
		constChar.obj = new Obj(Obj.Con, "", charType, constChar.getC(), 0);  	
	}
	public void visit(ConstantNum constNum) { 
		constNum.obj = new Obj(Obj.Con, "", intType, constNum.getN(), 0); 
	}
		
	public void visit(ConstTypeDeclaration ctDecl) { 
		lastConstType = ctDecl.getType().struct;
		Obj res = Tab.currentScope.findSymbol(ctDecl.getConstName());
		if(!ctDecl.getNumCharBoolConst().obj.getType().equals(lastConstType)) { 
			//report_error("Konstanta: " + ctDecl.getConstName() + " nije odgovarajuceg tipa.", ctDecl);
			list.add(new CompilerError(ctDecl.getLine(), "Konstanta: " + ctDecl.getConstName() + " nije odgovarajuceg tipa.", tipGreske));
		}
		if(res != null) { 
			//report_error("Redefinicija simbola " + ctDecl.getConstName(), ctDecl);
			list.add(new CompilerError(ctDecl.getLine(), "Redefinicija simbola " + ctDecl.getConstName(), tipGreske));
		} else { 
			ctDecl.obj = Tab.insert(Obj.Con, ctDecl.getConstName(), lastConstType );  
			ctDecl.obj.setAdr(ctDecl.getNumCharBoolConst().obj.getAdr());
		}	
	}
	    
	public void visit(ConstDeclaration ctDecl) { 
		Obj res = Tab.currentScope.findSymbol(ctDecl.getConstName());
		if(!ctDecl.getNumCharBoolConst().obj.getType().equals(lastConstType)) { 
			//report_error("Konstanta: " + ctDecl.getConstName() + " nije odgovarajuceg tipa.", ctDecl);
			list.add(new CompilerError(ctDecl.getLine(), "Konstanta: " + ctDecl.getConstName() + " nije odgovarajuceg tipa.", tipGreske));
		}
		if(res != null) { 
			//report_error("Redefinicija simbola " + ctDecl.getConstName(), ctDecl);
			list.add(new CompilerError(ctDecl.getLine(), "Redefinicija simbola " + ctDecl.getConstName(), tipGreske));
		} else { 
			ctDecl.obj = Tab.insert(Obj.Con, ctDecl.getConstName(), lastConstType );  
			ctDecl.obj.setAdr(ctDecl.getNumCharBoolConst().obj.getAdr());
		}	
	}
	    

	
	//************ IF, CONDITION ************************************//
	//IF
	public void visit(IfStmt ifstm) { 
		if(ifstm.getCondition().struct.getKind() != Struct.Bool) { 
			//report_error("Uslov mora biti logickog tipa Bool", ifstm);
			list.add(new CompilerError(ifstm.getLine(), "Uslov mora biti logickog tipa Bool", tipGreske));
		}
		//report_info("Prepoznat if!", ifstm);
	}
	//IF ELSE 
	public void visit(IfElseStmt ifstm) { 
		if(ifstm.getCondition().struct.getKind() != Struct.Bool) { 
			//report_error("Uslov mora biti logickog tipa Bool", ifstm);
			list.add(new CompilerError(ifstm.getLine(), "Uslov mora biti logickog tipa Bool", tipGreske));
		}
		//report_info("Prepoznat if else!", ifstm);
	}
	
	//Condition
	public void visit(ConditionList cl) { 
		if (cl.getCondition().struct.getKind() != Struct.Bool || cl.getCondTerm().struct.getKind() != Struct.Bool) { 
			//report_error("Oba operanda za operator OR moraju biti logickog tipa Bool", cl);
			list.add(new CompilerError(cl.getLine(), "Oba operanda za operator OR moraju biti logickog tipa Bool", tipGreske));
			cl.struct = Tab.noType;
		}else cl.struct = cl.getCondition().struct; 
	}
	public void visit(ConditionSingleTerm cst) { 
		if (cst.getCondTerm().struct.getKind() != Struct.Bool ) { 
			//report_error("Izraz mora biti logickog tipa Bool", cst);
			list.add(new CompilerError(cst.getLine(), "Izraz mora biti logickog tipa Bool", tipGreske));
			cst.struct = Tab.noType;
		}else cst.struct = cst.getCondTerm().struct; 
	}
	//CondTerm
	public void visit(ConditionTermList csf) { 
		if (csf.getCondFact().struct.getKind() != Struct.Bool || csf.getCondTerm().struct.getKind() != Struct.Bool) { 
			//report_error("Oba operanda za operator AND moraju biti logickog tipa Bool", csf);
			list.add(new CompilerError(csf.getLine(), "Oba operanda za operator AND moraju biti logickog tipa Bool", tipGreske));
			csf.struct = Tab.noType;
		}else csf.struct = csf.getCondFact().struct; 
	}
	public void visit(ConditionSingleFactor csf) { 
		if (csf.getCondFact().struct.getKind() != Struct.Bool) { 
			//report_error("Izraz mora biti logickog tipa Bool", csf);
			list.add(new CompilerError(csf.getLine(), "Izraz mora biti logickog tipa Bool", tipGreske));
			csf.struct = Tab.noType;
		}else csf.struct = csf.getCondFact().struct; 
	}
	
	
	//CondFact
	public void visit(CondFactSingle cf) { 
		cf.struct = cf.getExpression().struct;
	}
	
	public void visit(CondFactDouble cf) { 
		if(!cf.getExpression().struct.compatibleWith(cf.getExpression1().struct)) { 
			//report_error("Tipovi koji se porede moraju biti kompatibilni!", cf);
			list.add(new CompilerError(cf.getLine(), "Tipovi koji se porede moraju biti kompatibilni!", tipGreske));
			cf.struct = Tab.noType;
		} else { 
			if((cf.getExpression().struct.getKind() == Struct.Array || cf.getExpression1().struct.getKind() == Struct.Array) && (cf.getRelOp().obj.getKind() == Obj.Fld)) { 
				//report_error("Uz promenljive tipa niza mogu se koristiti samo == i !=", cf);
				list.add(new CompilerError(cf.getLine(), "Uz promenljive tipa niza mogu se koristiti samo == i !=", tipGreske));
				cf.struct = Tab.noType;
			} else { 
				cf.struct = boolType;
			}
		}
	}
	  
	public void visit(RelEqu rel) { 
		rel.obj = new Obj(Obj.Fld, "", Tab.nullType);                 //POSTAVIS MU DA JE KIND = FLD I ONDA PROVERAVAS ZA NIZOVE I KLASE RELACIONI OPERATOR SAMO == i !=
		rel.obj.setFpPos(Code.eq);									  // U POLJU FPPOS POSTAVLJENA OPERACIJA 
	}
	public void visit(RelNotEqu rel) { 
		rel.obj = new Obj(Obj.Fld, "", Tab.nullType);
		rel.obj.setFpPos(Code.ne);
	}
	public void visit(RelGreater rel) {                               //OSTALIM POSTAVI kind na NO_VALUE
		rel.obj = new Obj(Obj.NO_VALUE, "", Tab.nullType);
		rel.obj.setFpPos(Code.gt);
	}
	public void visit(RelGreaterE rel) { 
		rel.obj = new Obj(Obj.NO_VALUE, "", Tab.nullType);;
		rel.obj.setFpPos(Code.ge);
	}
	public void visit(RelLess rel) { 
		rel.obj = new Obj(Obj.NO_VALUE, "", Tab.nullType);
		rel.obj.setFpPos(Code.lt);
	}
	public void visit(RelLessE rel) { 
		rel.obj = new Obj(Obj.NO_VALUE, "", Tab.nullType);
		rel.obj.setFpPos(Code.le);
	}
	//ADDOP
	public void visit(OperAdd addop) { 
		(addop.obj = new Obj(Obj.NO_VALUE, "", Tab.nullType)).setFpPos(Code.add);;
	}
	public void visit(OperSub addop) { 
		(addop.obj = new Obj(Obj.NO_VALUE, "", Tab.nullType)).setFpPos(Code.sub);
	}
	public void visit(OperMul mulop) { 
		(mulop.obj = new Obj(Obj.NO_VALUE, "", Tab.nullType)).setFpPos(Code.mul);
	}
	public void visit(OperDiv mulop) { 
		(mulop.obj = new Obj(Obj.NO_VALUE, "", Tab.nullType)).setFpPos(Code.div);
	}
	public void visit(OperMod mulop) { 
		(mulop.obj = new Obj(Obj.NO_VALUE, "", Tab.nullType)).setFpPos(Code.rem);
	}
	
	
	
	
	//public boolean passed() { 
	//	return !errorDetected ;
	//}
	  // *******************************Oporavak od gresaka za smene sa neterminalima pre error simbola *******************************
	  
	  public void visit(VarError eVar) { 
		// report_error("OPORAVAK DO , PRI DEFINICIJI PROMENLJIVE! ", eVar);
		  list.add(new CompilerError(eVar.getLine(), "OPORAVAK DO , PRI DEFINICIJI PROMENLJIVE! ", tipGreske));
	  }
	  
	  public void visit(ErrorDesignator eVar) { 
		 // report_error("OPORAVAK DO ; U IZRAZU DODELE! ", eVar);
		  list.add(new CompilerError(eVar.getLine(), "OPORAVAK DO ; U IZRAZU DODELE! ", tipGreske));
	  }

  
}

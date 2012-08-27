package FindEscape;

/***********************************************************************************************************
 *   CLASSE Escape                                                                                         *
 * ------------------------------------------------------------------------------------------------------- *
 *   Guarda informa��es sobre onde as vari�veis foram declaradas e sobre se escapam ou n�o.                *
 ***********************************************************************************************************/

abstract class Escape
{
  int depth;
  abstract void setEscape();
}


/***********************************************************************************************************
 *   CLASSE FormalEscape                                                                                   *
 * ------------------------------------------------------------------------------------------------------- *
 *   Guarda informa��es sobre em que n�vel os par�metros de uma fun��o foram declarados e se escapam ou    *
 *   n�o.                                                                                                  *
 ***********************************************************************************************************/

class FormalEscape extends Escape
{
  Absyn.FieldList fl;

  FormalEscape (int d, Absyn.FieldList f) {
    depth=d; fl=f; fl.escape = false;
  }
  void setEscape() {fl.escape=true;}
}


/***********************************************************************************************************
 *   CLASSE FormalEscape                                                                                   *
 * ------------------------------------------------------------------------------------------------------- *
 *   Guarda informa��es sobre em que n�vel uma vari�vel foi declarada e se escapa ou n�o.                  *
 ***********************************************************************************************************/

// Classe VarEscape: armazena uma vari�vel e a profundidade em que foi declarada.
class VarEscape extends Escape
{
  Absyn.VarDec vd;

  VarEscape (int d, Absyn.VarDec v) {
    depth=d; vd=v; vd.escape=false;
  }
  void setEscape() {vd.escape=true;}
}


/***********************************************************************************************************
 *   CLASSE FindEscape                                                                                     *
 * ------------------------------------------------------------------------------------------------------- *
 *   Percorre a �rvore sint�tica abstrata determinando quais as vari�veis e par�metros que escapam e       *
 *   quais n�o.                                                                                            *
 ***********************************************************************************************************/

public class FindEscape
{
  Symbol.Table escEnv = new Symbol.Table();      // tabela de s�mbolos

  public FindEscape (Absyn.Exp e) { traverseExp(0,e); }

  // traverseVar: trata vari�veis.
  void traverseVar (int depth, Absyn.Var v) {
    if (v instanceof Absyn.SimpleVar)
      traverseVar (depth, (Absyn.SimpleVar) v);
    else if (v instanceof Absyn.FieldVar)
      traverseVar (depth, (Absyn.FieldVar) v);
    else if (v instanceof Absyn.SubscriptVar)
      traverseVar (depth, (Absyn.SubscriptVar) v);
    else throw new Error("transVar");
  }

  void traverseVar (int depth, Absyn.SimpleVar v) {
    Escape escape = (Escape) escEnv.get(v.name);
    // verifica se esta vari�vel ou par�metro formal est� sendo usada em um n�vel mais interno ao da sua
    // declara��o
    if ((escape != null) && (depth > escape.depth))
      escape.setEscape();  // esta vari�vel/par�metro escapa (precisar� ficar no frame, n�o em registrador)
  }

  void traverseVar (int depth, Absyn.FieldVar v) {
    traverseVar (depth, v.var);
  }

  void traverseVar (int depth, Absyn.SubscriptVar v) {
    traverseVar (depth, v.var);
  }

  // traverseExp: trata express�es.
  void traverseExp (int depth, Absyn.Exp e) {
    if ( (e instanceof Absyn.NilExp) ||
	 (e instanceof Absyn.IntExp) ||
	 (e instanceof Absyn.StringExp) ||
	 (e instanceof Absyn.RecordExp) ||
	 (e instanceof Absyn.BreakExp) )
	// estes n�o trabalham com vari�veis e por isso n�o ser�o analisados
      return;
    if (e instanceof Absyn.VarExp)
      traverseExp (depth, (Absyn.VarExp) e);
    else if (e instanceof Absyn.CallExp)
      traverseExp (depth, (Absyn.CallExp) e);
    else if (e instanceof Absyn.OpExp)
      traverseExp (depth, (Absyn.OpExp) e);
    else if (e instanceof Absyn.SeqExp)
      traverseExp (depth, (Absyn.SeqExp) e);
    else if (e instanceof Absyn.AssignExp)
      traverseExp (depth, (Absyn.AssignExp) e);
    else if (e instanceof Absyn.IfExp)
      traverseExp (depth, (Absyn.IfExp) e);
    else if (e instanceof Absyn.WhileExp)
      traverseExp (depth, (Absyn.WhileExp) e);
    else if (e instanceof Absyn.ForExp)
      traverseExp (depth, (Absyn.ForExp) e);
    else if (e instanceof Absyn.LetExp)
      traverseExp (depth, (Absyn.LetExp) e);
    else if (e instanceof Absyn.ArrayExp)
      traverseExp (depth, (Absyn.ArrayExp) e);
    else throw new Error("transExp");
  }

  void traverseExp (int depth, Absyn.VarExp e) {
    traverseVar (depth, e.var);
  }

  void traverseExp (int depth, Absyn.CallExp e) {
    for (Absyn.ExpList arg=e.args; arg!=null; arg=arg.tail)
      traverseExp (depth, arg.head);
  }

  void traverseExp (int depth, Absyn.OpExp e) {
    traverseExp (depth, e.left);
    traverseExp (depth, e.right);
  }

  void traverseExp (int depth, Absyn.SeqExp e) {
    for (Absyn.ExpList exp=e.list; exp!=null; exp=exp.tail)
      traverseExp (depth, exp.head);
  }

  void traverseExp (int depth, Absyn.AssignExp e) {
    traverseVar (depth, e.var);
    traverseExp (depth, e.exp);
  }

  void traverseExp (int depth, Absyn.IfExp e) {
    traverseExp (depth, e.test);
    traverseExp (depth, e.thenclause);
    if (e.elseclause != null)
      traverseExp (depth, e.elseclause);
  }

  void traverseExp (int depth, Absyn.WhileExp e) {
    traverseExp (depth, e.test);
    traverseExp (depth, e.body);
  }

  void traverseExp (int depth, Absyn.ForExp e) {
    escEnv.beginScope();
    traverseDec (depth, e.var);
    traverseExp (depth, e.hi);
    traverseExp (depth, e.body);
    escEnv.endScope(); 
 }

  void traverseExp (int depth, Absyn.LetExp e) {
    escEnv.beginScope();
    for (Absyn.DecList d=e.decs; d!=null; d=d.tail)
      traverseDec (depth, d.head);
    traverseExp (depth, e.body);
    escEnv.endScope();
  }

  void traverseExp (int depth, Absyn.ArrayExp e) {
    traverseExp (depth, e.size);
    traverseExp (depth, e.init);
  }

  // traverseDec: trata declara��es.
  void traverseDec (int depth, Absyn.Dec d) {
    if (d instanceof Absyn.VarDec)
      traverseDec (depth, (Absyn.VarDec) d);
    else if (d instanceof Absyn.FunctionDec)
      traverseDec (depth, (Absyn.FunctionDec) d);
    else if (!(d instanceof Absyn.TypeDec))
      throw new Error("transDec");
  }

  void traverseDec (int depth, Absyn.VarDec d) {
    // declara uma vari�vel e armazena a profundidade e a fun��o dentro da qual foi declarada
    escEnv.put (d.name, new VarEscape(depth,d));
  }

  void traverseDec (int depth, Absyn.FunctionDec d) {
    for (Absyn.FunctionDec dd=d; dd!=null; dd=dd.next) {
      escEnv.beginScope();
      for (Absyn.FieldList p=dd.params; p!=null; p=p.tail)
        // declara um par�metro formal e armazena a profundidade em que foi declarado
	escEnv.put (p.name, new FormalEscape(depth+1,p));
      traverseExp (depth+1, dd.body);
      escEnv.endScope();
    }
  }
}

package Set;

import java.util.Hashtable;
import java.util.Dictionary;
import List.*;


/***********************************************************************************************************
 *   CLASSE PairOrderedSet                                                                                 *
 * ------------------------------------------------------------------------------------------------------- *
 *   Implementa um conjunto formado por pares.                                                             *
 *                                                                                                         *
 *   OBS: este conjunto pode comportar-se como uma fila ou pilha, se forem usados os m�todos addFirst      *
 *        e addLast ao inv�s do m�todo add.                                                                *
 ***********************************************************************************************************/

public class PairOrderedSet
{
  /* Detalhes sobre a implementa��o:
   *    Ser�o usadas duas estruturas: uma lista duplamente ligada e uma tabela de hash. Na tabela de hash,
   *    cada elemento elemento x do par (x,y) estar� associado a um conjunto e a uma tabela de hash:
   *       - o conjunto estar� formado pelos elementos y com os quais forma um par;
   *       - a tabela de hash dir� em que posi��o o par (x,y) est� na lista duplamente ligada.
   */
  private DoubleLinkedList first, last;
  Dictionary dictionary;
  int size;
  String name;
  public boolean show = false;
  public boolean showErro = true;

  /* construtor PairOrderedSet()
   *    cria um conjunto vazio, sem nome.
   */
  public PairOrderedSet() {
    this((String)null);
  }

  /* construtor PairOrderedSet (String name)
   *    cria um conjunto vazio, com o nome especificado.
   */
  public PairOrderedSet (String setName) {
    name = setName;
    initialize();
  }

  /* construtor PairOrderedSet (PairList l)
   *    cria um conjunto a partir de uma lista de pares.
   */
  public PairOrderedSet (PairList l) {
    initialize();
    for ( ; l!=null; l=l.getNext())
      addLast(l.getInfoX(),l.getInfoY());
  }

  /* initialize()
   *    inicializa o conjunto (o esvazia)
   */
  public void initialize() {
    first = null;
    last = null;
    size = 0;
    dictionary = new Hashtable();
  }

  /* boolean isEmpty()
   *    verifica se o conjunto est� vazio.
   */
  public boolean isEmpty() {return (size==0);}

  /* int size()
   *    retorna o n�mero de elementos deste conjunto.
   */
  public int size() {return size;}

  /* PairOrderedSet makeCopy()
   *    retorna uma c�pia deste conjunto.
   */
  public PairOrderedSet makeCopy() {
    PairOrderedSet set = new PairOrderedSet();
    for (DoubleLinkedList p=first; p!=null; p=p.getNext()) {
      Pair q = (Pair)p.getInfo();
      set.add(q.x,q.y);
    }
    return set;
  }

  /* boolean add (Object x, Object y)
   *    acrescenta um par (x,y) ao conjunto (por default, no final dele).
   */
  public boolean add (Object x, Object y) {return addLast(x,y);}

  /* boolean addFirst (Object x, Object y)
   *    acrescenta um par (x,y) no in�cio do conjunto; retorna false se o elemento pertence a este conjunto,
   *    e true caso contr�rio.
   */
  public boolean addFirst (Object x, Object y) {
    if ((show) && (name!=null)) System.out.println("Inserindo ("+x+","+y+") em "+this);
    if (x == null || y == null)
      return false;
    Pair P = (Pair) dictionary.get(x);
    if (P != null && ((OrderedSet)P.x).has(y)) {
      if ((showErro||show) && (name!=null)) System.out.println("ERRO: ("+x+","+y+") j� est� em "+this);
      return false;
    }
    else if (P == null) {
      P = new Pair(new OrderedSet(), new Hashtable());
      dictionary.put(x,P);
    }
    OrderedSet set = (OrderedSet) P.x;
    Dictionary dict = (Dictionary) P.y;
    set.add(y);
    DoubleLinkedList added = new DoubleLinkedList(new Pair(x,y),null,first);
    if (last == null)
      last = added;
    else
      first.setPrev(added);
    first = added;
    dict.put(y,added);
    size++;
    if (show && name!=null) System.out.println("Inserido ("+x+","+y+") em "+this);
    return true;
  }

  /* boolean addLast (Object x, Object y)
   *    acrescenta um par (x,y) ao conjunto, no final; retorna false se o elemento pertence a este conjunto,
   *    e true caso contr�rio.
   */
  public boolean addLast (Object x, Object y) {
    if (show && name!=null) System.out.println("Inserindo ("+x+","+y+") em "+this);
    if (x == null || y == null)
      return false;
    Pair P = (Pair) dictionary.get(x);
    if (P != null && ((OrderedSet)P.x).has(y)) {
      if ((showErro||show) && (name!=null)) System.out.println("ERRO: ("+x+","+y+") j� est� em "+this);
      return false;
    }
    else if (P == null) {
      P = new Pair(new OrderedSet(), new Hashtable());
      dictionary.put(x,P);
    }
    OrderedSet set = (OrderedSet) P.x;
    Dictionary dict = (Dictionary) P.y;
    set.add(y);
    DoubleLinkedList added = new DoubleLinkedList(new Pair(x,y),last,null);
    if (first == null)
      first = added;
    else
      last.setNext(added);
    last = added;
    dict.put(y,added);
    size++;
    if (show && name!=null) System.out.println("Inserido ("+x+","+y+") em "+this);
    return true;
  }

  /* boolean remove (Object x, Object y)
   *    remove o par (x,y) deste conjunto; retorna false se o elemento n�o pertence a este conjunto, e true
   *    caso contr�rio.
   */
  public boolean remove (Object x, Object y) {
    if (show && name!=null) System.out.println("Removendo ("+x+","+y+") de "+this);
    if ( (x == null) || (y == null) || (size == 0) )
      return false;
    Pair P = (Pair) dictionary.get(x);
    if (P == null) {
      if ((showErro||show) && (name!=null)) System.out.println("ERRO: ("+x+","+y+") n�o est� em "+this);
      return false;
    }
    OrderedSet set = (OrderedSet) P.x;
    Dictionary dict = (Dictionary) P.y;
    DoubleLinkedList element = (DoubleLinkedList) dict.get(y);
    if (element == null) {
      if ((showErro||show) && (name!=null)) System.out.println("ERRO: ("+x+","+y+") n�o est� em "+this);
      return false;
    }

    if (element.getPrev() != null)
      element.getPrev().setNext(element.getNext());
    else
      first = element.getNext();

    if (element.getNext() != null)
      element.getNext().setPrev(element.getPrev());
    else
      last = element.getPrev();

    dict.remove(y);
    set.remove(y);
    size--;
    if (show && name!=null) System.out.println("Removido ("+x+","+y+") de "+this);
    return true;
  }

  /* boolean has (Object x, Object y)
   *    verifica se o par (x,y) pertence a este conjunto.
   */
  public boolean has (Object x, Object y) {
    if ( (x==null) || (y==null) || (size==0) )
      return false;
    Pair P = (Pair) dictionary.get(x);
    if (P == null)
      return false;
    Dictionary dict = (Dictionary) P.y;
    return (dict.get(y) != null);
  }

  /* PairOrderedSet union (PairOrderedSet)
   *    altera este conjunto, fazendo a uni�o com o conjunto fornecido; retorna este conjunto.
   */
  public PairOrderedSet union (PairOrderedSet set) {
    if (set==null)
      return null;
    for (DoubleLinkedList p=set.first; p!=null; p=p.getNext()) {
      Pair q = (Pair)p.getInfo();
      addLast(q.x,q.y);
    }
    return this;
  }

  /* PairOrderedSet intersection (PairOrderedSet)
   *    altera este conjunto, fazendo a intersec��o com o conjunto fornecido; retorna este conjunto.
   */
  public PairOrderedSet intersection (PairOrderedSet set) {
    if (set==null)
      return null;
    for (DoubleLinkedList p=first; p!=null; p=p.getNext()) {
      Pair q = (Pair)p.getInfo();
      if (!set.has(q.x,q.y))
	remove(q.x,q.y);
    }
    return this;
  }

  /* PairOrderedSet difference (PairOrderedSet)
   *    altera este conjunto, removendo os elementos deste conjunto presentes no conjunto fornecido;
   *    retorna este conjunto.
   */
  public PairOrderedSet difference (PairOrderedSet set) {
    if (set==null)
      return null;
    for (DoubleLinkedList p=first; p!=null; p=p.getNext()) {
      Pair q = (Pair)p.getInfo();
      if (set.has(q.x,q.y))
	remove(q.x,q.y);
    }
    return this;
  }

  /* boolean isEqualTo (PairOrderedSet set)
   *    verifica se este conjunto tem os mesmos elementos que o fornecido.
   */
  public boolean isEqualTo (PairOrderedSet set) {
    if (set==null || size!=set.size)
      return false;
    for (DoubleLinkedList p=first; p!=null; p=p.getNext()) {
      Pair q = (Pair)p.getInfo();
      if (!set.has(q.x,q.y))
	return false;
    }
    for (DoubleLinkedList p=set.first; p!=null; p=p.getNext()) {
      Pair q = (Pair)p.getInfo();
      if (!has(q.x,q.y))
	return false;
    }
    return true;
  }

  /* OrderedSet get (Object x)
   *    devolve o conjunto com todos os elementos y para os quais exista um par (x,y) neste conjunto.
   */
  public OrderedSet get (Object x) {
    if (x == null)
      return null;
    Pair P = (Pair) dictionary.get(x);
    if (P != null)
      return (OrderedSet)P.x;
    return new OrderedSet();
  }

  /* Pair getFirst()
   *    retorna o primeiro par (x,y) deste conjunto.
   */
  public Pair getFirst() {
    if (first == null)
      return null;
    return (Pair) first.getInfo();
  }

  /* Pair getLast()
   *    retorna o �ltimo par (x,y) deste conjunto.
   */
  public Pair getLast() {
    if (last == null)
      return null;
    return (Pair) last.getInfo();
  }

  /* Pair getPrev (Object x, Object y)
   *    retorna o par anterior ao par (x,y) deste conjunto.
   */
  public Pair getPrev (Object x, Object y) {
    if ( (x == null) || (y == null) || (size == 0) )
      return null;
    Pair P = (Pair) dictionary.get(x);
    if (P == null)
      return null;
    Dictionary dict = (Dictionary) P.y;
    DoubleLinkedList element = (DoubleLinkedList) dict.get(y);
    if (element == null || element.getPrev() == null)
      return null;
    return (Pair) element.getPrev().getInfo();
  }

  /* Pair getNext (Object x, Object y)
   *    retorna o par seguinte ao par (x,y) deste conjunto.
   */
  public Pair getNext (Object x, Object y) {
    if ( (x == null) || (y == null) || (size == 0) )
      return null;
    Pair P = (Pair) dictionary.get(x);
    if (P == null)
      return null;
    Dictionary dict = (Dictionary) P.y;
    DoubleLinkedList element = (DoubleLinkedList) dict.get(y);
    if (element == null || element.getNext() == null)
      return null;
    return (Pair) element.getNext().getInfo();
  }

  /* String toString()
   *    retorna uma String com a representa��o deste conjunto (nome do conjunto + elementos).
   */
  public String toString() {
    String s = "";
    if (name != null && name != "")
      s = name+": ";
    s = s+"{";
    DoubleLinkedList p=first;
    if (p == null)
      return s+"}";
    s = s+(Pair)p.getInfo();
    for (p=p.getNext(); p!=null; p=p.getNext())
      s = s+", "+(Pair)p.getInfo();
    return s+"}";
  }
}

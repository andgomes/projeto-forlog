import java.util.*;

/**
 * Classe que processa uma String de formula, valida e constroi 
 * uma arvore sintatica utilizando a estrutura de dados No
 * @see No
 */

public class Formula {

	private String formulaRaw;
	private No arvore;
	private boolean valor;
	private int qtdAtomos;
	private Atomo atomos[];
	private String formulaPassada;
	
	/**
	 * Construtor que recebe uma String representando a formula logica, valida 
	 * e cria um arvore sintatica utilizando a estrutura de dados No
	 * @param formulaRaw String da formula
	 */

	public Formula(String formulaRaw) {
	
		this.formulaRaw = formulaRaw;
		formulaPassada = formulaRaw;
		String formula = formulaRaw.replace(" ","").toLowerCase();
		
		if (!isValida(formula)) {
			throw new IllegalArgumentException("A formula não é válida:\n" + 
			        formula);
        }
        
		arvore = criarArvore(formula);
		valor = avaliarArvore();

	}

	private No criarArvore(String formulaComParenteses) {
	
		String formula = formulaComParenteses;
		
		if (formulaComParenteses.charAt(0) == '(') { //Remove parenteses inicial e final da formula.
			formula = formulaComParenteses.substring(1, 
			        formulaComParenteses.length() - 1);
		}
			
		if(formula.length() == 1) { //Formula é um átomo
		
			Atomo a1 = recuperarAtomo(formula.charAt(0));
			No<Atomo> atomo = new No<>(a1);
			return atomo;

		}
		
		int parenteses = 0, atual = -1;
		No raiz = null;
		
		do {//Delimita 1º operando do operador principal
		
			atual++;
			
			if (formula.charAt(atual) == '(') {
				parenteses++;
			}
				
			if (formula.charAt(atual) == ')') {
				parenteses--;
			}	

		} while (parenteses > 0);
		
		switch (formula.charAt(atual)) { //Verifica operador principal
		
			case '\u223c': //NEGACAO
			
			    ConectivoUnario c2 = new ConectivoUnario(TipoConectivo.NEGACAO);
				raiz = new No<ConectivoUnario>(c2, 
				        criarArvore( formula.substring(atual + 1) ) );//Cria arvore do operando e a anexa a sua árvore
			    break;
			    
			default: //Outros casos (Binário)
			
				atual++;//Vai para o operador Binário
				
				if (atual >= formula.length()) {
					return criarArvore(formula.substring(0, formula.length()));//Caso seja formula com vários parenteses aninhados
				}
					
				ConectivoBinario conectivo = null;
				
				switch(formula.charAt(atual)) {
				
					case '\u2227': //E
					
						conectivo = new ConectivoBinario(TipoConectivo.E);
					    break;

					case '\u2228': //OU
					
						conectivo = new ConectivoBinario(TipoConectivo.OU);
					    break;
					    
					case '\u2192': //SE
					
						conectivo = new ConectivoBinario(TipoConectivo.SE_ENTAO);
					    break;
					    
					case '\u2194': //SSE
					
						conectivo = new ConectivoBinario(TipoConectivo.SSE);
					    break;
					    
					default:
					
						conectivo = new ConectivoBinario(TipoConectivo.OU);
					    break;
				}
				
				No esquerda = criarArvore(formula.substring(0, atual)); // Criar arvore do operando à esquerda
				No direita = criarArvore(formula.substring(atual + 1)); // Criar arvore do operando à esquerda
				raiz = new No<ConectivoBinario>(conectivo, esquerda, direita); //Cria arvore atual com o Conectivo como nó e operandos à esquerda e direita como filhos.
			    break;
		}
		
		return raiz;
		
	}

	private Atomo recuperarAtomo(char rotulo) {
	
		for (int i = 0; i < qtdAtomos; i++) {
		    if (atomos[i].getRotulo() == rotulo) {
				return atomos[i];
			}	
		}		
				
		return null;

	}

	private boolean isValida(String formula) {
	
		if (formula.charAt(0) != '(' || 
		        formula.charAt(formula.length()-1) != ')') {
			return false;
		}	//Verifica se ela possui os parents no começo e fim
		
		int abreParentese = 0, fechaParentese = 0;
		Set<Character> proposicoes = new HashSet<>();

		for (int i = 0; i < formula.length(); i++) {
		
			char atual = formula.charAt(i);
			
			if (atual != ')' && atual != '(' && 
			        (!Atomo.isAtomo(atual)) && 
			        (!Conectivo.isConectivoValido(atual))) { // não for um character válido 
				return false;
			}	

			if (atual == '(') {
			
				abreParentese++;
				
				if (formula.charAt(i + 1) == ')') {//Parenteses vazio tipo:()
					return false;
				}	

			}
			
			if (atual == ')') {
				fechaParentese++;
			}	
			
			if (fechaParentese > abreParentese) {//Se fechou algum parêntese a mais
				return false;
			}	
			
			if (Atomo.isAtomo(atual)) { //É proposição
			
				proposicoes.add(atual); // Adiciona no HashSet que faz o controle de proposições
				
				if (proposicoes.size() > 5) { 
					return false;
				}
					
				char proximo = formula.charAt(i+1);
				
				if (Atomo.isAtomo(proximo)) { //É proposição
					return false;
				}
					
				if (proximo == '(') {// Proposição sem operando ou termino
					return false;
				}	
				
			}
			
			if (Conectivo.isConectivoBinarioValido(atual)) { // É operador binário
			        
				char anterior = formula.charAt(i - 1);
				char proximo = formula.charAt(i + 1);
				
				if (proximo == ')' || Conectivo.isConectivoValido(proximo)) { //Operador sem operando à direita
					return false;
				}
					
				if (anterior == '(' || Conectivo.isConectivoValido(anterior)) { //Operador sem operando à esquerda
					return false;
				}	

			}
			
			if (Conectivo.isConectivoUnarioValido(atual)) { //É operador Unário (negação)
			
				if (formula.charAt(i - 1) != '(') {
					return false;
				}
					
				char proximo = formula.charAt(i + 1);
				
				if (proximo == ')' || Conectivo.isConectivoValido(proximo)) { //Operador sem operando
					return false;
				}	

			}
			
		}

		if (abreParentese != fechaParentese) { //Verifica se há a mesma quantidade de parenteses
			return false;
		}
			
		qtdAtomos = proposicoes.size();
		atomos = new Atomo[qtdAtomos];//Cria vetor com átomos usados na formula

		int i = 0;
		
		for (Character rotulo : proposicoes) {
		
			atomos[i] = new Atomo(rotulo);// Guarda átomos na formula
			i++;

		}

		return true;

	}
	
	/**
	 * Retorna o valor final da avaliacao da arvore sintatica 
	 * construida a partir da formula passada para o construtor
	 * @return valor booleano representando o resultado da expressao
	 */

	public boolean getValor() {
	    return valor;
	}
	
	/**
	 * Retorna a quantidade de Atomos presentes na formula
	 * @return int representando a quantidade de atomos presentes na formula
	 */
	
	public int getQuantidadeAtomos() {
		return qtdAtomos;
	}
	
	/**
	 * Retorna o rotulo de um atomo presente na formula
	 * @param i indice do atomo na formula
	 * @return caractere do rotulo do atomo na formula
	 */

	public char getProposicao(int i) {
	
		if (i >= 0 && i < qtdAtomos) {
			return atomos[i].getRotulo();
		}	
		return ' ';

	}
	
	/**
	 * Aceita um caractere representando o rotulo de um atomo e retorna 
	 * o valor logico do atomo representado por aquele rotulo
	 * @param rotulo caractere do atomo a ser retornado o valor logico
	 * @return valor logico do atomo representado pelo rotulo
	 */
	
	public boolean getValorProposicao(char rotulo) {
	
		for (int i = 0; i < qtdAtomos; i++) {
		
			if (atomos[i].getRotulo() == rotulo) {
				return atomos[i].getValor();
            }
            
		}
				
		throw new IllegalArgumentException("Átomo não existente");

	}

    /**
     * Modifica o valor de um atomo procurando pelo caractere do rotulo
     * @param rotulo caractere representando o rotulo 
     *        do atomo a ter o valor modificado
     * @param valor novo valor logico do atomo
     */

	public void setValorProposicao(char rotulo, boolean valor) {
	
		for (int i = 0; i < qtdAtomos; i++) {

			if (atomos[i].getRotulo() == rotulo) {
			
				if (atomos[i].getValor() != valor) {
				
					atomos[i].setValor(valor);
					avaliarArvore();

				}
				
				return;
				
			}

		}
			
		throw new IllegalArgumentException("Átomo não existente");	

	}
	
	/**
     * Modifica o valor de um atomo procurando pelo indice do atomo
     * @param i indice representando o atomo na formula a ter o valor modificado
     * @param valor novo valor logico do atomo
     */
	
	public void setValorIndex(int i, boolean valor) {
	
		if (i >= 0 && i < qtdAtomos) {
		
			if (atomos[i].getValor() != valor) {	
				atomos[i].setValor(valor);
				avaliarArvore();
			}

		}

	}
	
	/**
	 * Metodo que avalia a arvore sintatica da formula e retorna seu valor
	 * @return valor booleano da formula
	 */
	
	public boolean avaliarArvore() {
	
		valor = arvore.avaliar();
		return valor;

	}

    /**
     * Metodo que imprime no console as informacoes da formula
     */

	public void show() {
	
		System.out.println(formulaPassada);
		
		for (int i = 0; i < qtdAtomos; i++) {
			System.out.println(atomos[i]);
		}
			
		System.out.println("Valor Formula: " + valor + 
		        "\n-------------------------------" + 
		        "----------------------------");

	}
	
	/**
	 * Retorna a String que representa a formula logica
	 * @return representacao String da formula
	 */
	
	public String getFormula() {
		return formulaRaw;
	}
	
	/**
	 * Metodo utilizado pela classe DrawTree para 
	 * desenhar a arvore sintatica da formula
	 */
	
	public void draw() {
		DrawTree desenho = new DrawTree(this.getFormula(),this.arvore,30);
	}
	
} // fim da classe Formula

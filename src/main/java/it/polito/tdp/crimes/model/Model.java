package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	private SimpleWeightedGraph<String, DefaultWeightedEdge> grafo;
	private EventsDao dao;
	private List<String> vertici;
	private List<Adiacenza> adiacenze;
	private boolean grafoCreato;
	
	private List<String> percorsoBest;
	private Integer pesoMin; //peso del percorso
	
	public Model() {
		this.dao = new EventsDao();
		this.vertici= new ArrayList<>();
		this.adiacenze= new ArrayList<>();
		this.grafoCreato=false;
		this.percorsoBest= new ArrayList<>();
	}
	
	public void creaGrafo(String categoria, int anno) {
		this.grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		this.vertici= this.dao.getVertici(categoria,anno);
		Graphs.addAllVertices(grafo, this.vertici);
		this.adiacenze= this.dao.getAdiacenze(categoria, anno);
		for (Adiacenza a : this.adiacenze) {
			if (grafo.vertexSet().contains(a.getT1()) && grafo.vertexSet().contains(a.getT2()) && a.getPeso()>0) {
				Graphs.addEdgeWithVertices(grafo, a.getT1(), a.getT2(), (double)a.getPeso());
			}
		}
		this.grafoCreato=true;
	}
	
	public boolean isGrafoCreato() {
		return grafoCreato;
	}

	public int getNumVertici() {
		return grafo.vertexSet().size();
	}
	
	public int getNumArchi() {
		return grafo.edgeSet().size();
	}

	public Set<String> getVertici(){
		return grafo.vertexSet();
	}
	
	public List<Adiacenza> getArchiConPesoMax(){
		List<Adiacenza>  ris= new ArrayList<>();
		int max=Integer.MIN_VALUE;
		for (Adiacenza a : this.adiacenze) {
			if(a.getPeso()>max)
				max=a.getPeso();
		}
		for (Adiacenza a : this.adiacenze) {
			if(a.getPeso()==max)
				ris.add(a);
		}
		return ris;
	}

	public List<Integer> getAllYears() {
		return dao.getAllYears();
	}
	
	public List<String> getAllCategoriesTypes(){
		return dao.getAllCategoriesTypes();
	}
	
	public List<String> percorsoMigliore(String partenza, String arrivo){ 
		this.percorsoBest=null; 
		this.pesoMin=0; 
		
		List<String> parziale= new ArrayList<>();
		parziale.add(partenza); 
		ricorsione(parziale , arrivo); 
		return this.percorsoBest;
	}
	private void ricorsione(List<String> parziale, String arrivo){
		String ultimo = parziale.get(parziale.size()-1);
		if (ultimo.equals(arrivo) && parziale.size()==this.getNumVertici()){ 
			System.out.println("Ciao1");
			int minimo= this.calcolaPeso(parziale);
			if(this.percorsoBest==null || minimo<this.pesoMin){ //prima iterazione o ho trovato una soluzione migliore
				System.out.println("Ciao2");
				this.pesoMin=minimo;
				this.percorsoBest= new ArrayList<>(parziale);
				return;
			}
			else //ho trovato una soluzione ma non è la migliore
				return;
		}
		//da qui faccio la ricorsione:
		for(DefaultWeightedEdge e : grafo.edgesOf(ultimo)){ 
			String prossimo= Graphs.getOppositeVertex(grafo, e, ultimo); //prendo il prossimo vertice che voglio testare
			if (!parziale.contains(prossimo)){ //per evitare i cicli 
				parziale.add(prossimo); 
				ricorsione(parziale,arrivo);
				parziale.remove(parziale.size()-1); //backtracking
			}
		}
	}
	
	private Integer calcolaPeso(List<String> parziale) { 
		int peso=0;
		int i=0; //indice che mi serve per prendere il match successivo in parziale
		for (String s : parziale) {
			if (i==(parziale.size()-1)) 
				break;
			DefaultWeightedEdge e = grafo.getEdge(s, parziale.get(i+1));
			i++;
			peso += grafo.getEdgeWeight(e);
		}
		return peso;
	}

	public Integer getPesoMin() {
		return pesoMin;
	}
	
	
	
}

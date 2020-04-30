package eg.edu.alexu.csd.filestructure.btree;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchEngine implements ISearchEngine {
	/*How to define minDegree ??*/
	private BTree<String , String> tree;
	public SearchEngine(int minDegree){
		if(minDegree <2)
			throw new RuntimeErrorException(new ExceptionInInitializerError());
		tree = new BTree<>(minDegree);
	}
	@Override
	public void indexWebPage(String filePath) {
		/*if a null or a file with an empty name is entered*/
		if(filePath == null || filePath.equals(""))
			throw new RuntimeErrorException(new ExceptionInInitializerError());
		File file = new File(filePath);
		/*if the file does not exist*/
		if(!file.exists())
			throw new RuntimeErrorException(new ExceptionInInitializerError());
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList list = document.getElementsByTagName("doc");
			for(int i = 0 ; i < list.getLength() ; i++){
				Node node = list.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE){
					Element element = (Element) node;
					tree.insert(element.getAttribute("id"),element.getTextContent());
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void indexDirectory(String directoryPath) {
		/*if a null or a directory with an empty name is entered*/
		if(directoryPath == null || directoryPath.equals(""))
			throw new RuntimeErrorException(new ExceptionInInitializerError());
		File directory = new File(directoryPath);
		/*if the directory does not exist or the name refers to a file not a directory*/
		if(!directory.exists() || directory.isFile())
			throw new RuntimeErrorException(new ExceptionInInitializerError());
		/*It gives a list of all files and directories in the directory.*/
		String[] fileList = directory.list();
		if(fileList!= null) {
			for (String s : fileList) {
				File f = new File(s);
				if (f.isDirectory()) {
					indexDirectory(s);
				} else  {
					indexWebPage(directoryPath+"\\"+s);
				}
			}
		}
	}

	@Override
	public void deleteWebPage(String filePath) {
		/*if a null or a file with an empty name is entered*/
		if(filePath == null || filePath.equals(""))
			throw new RuntimeErrorException(new ExceptionInInitializerError());
		File file = new File(filePath);
		/*if the file does not exist*/
		if(!file.exists())
			throw new RuntimeErrorException(new ExceptionInInitializerError());
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList list = document.getElementsByTagName("doc");
			for(int i = 0 ; i < list.getLength() ; i++){
				Node node = list.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE){
					Element element = (Element) node;
					tree.delete(element.getAttribute("id"));
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<ISearchResult> searchByWordWithRanking(String word) {
		if(word == null )throw new RuntimeErrorException(null);
		List<ISearchResult> re= new ArrayList<>();
		if(tree.getRoot() == null||word.equals(""))
			return re;
        tree.searchInValue(tree.getRoot(),true,word,re);
        System.out.println(re.size()+"  SIZE ");
		return re;
	}

	@Override
	public List<ISearchResult> searchByMultipleWordWithRanking(String sentence) {
		if(sentence == null )throw new RuntimeErrorException(null);
		List<ISearchResult> re= new ArrayList<>();
		if(tree.getRoot() == null||sentence.equals(""))
			return re;
		tree.searchInValue(tree.getRoot(),false,sentence,re);
		System.out.println(re.size()+"  SIZE ");
		return re;
	}



}

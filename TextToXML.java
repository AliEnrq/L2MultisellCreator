import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Comment;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextToXML {

    public static void main(String[] args) {
        try {
            // Ruta del archivo de texto de entrada con los datos de los ids
            String inputFilePath = "input.txt";  // Cambia esto a la ruta de tu archivo de texto
            // Ruta del archivo con el nombre de los items y sus ids
            String namesFilePath = "item_names.txt";  // Cambia esto a la ruta del archivo con los nombres

            // Crear el documento XML
            Document document = createXMLDocument(inputFilePath, namesFilePath);

            // Convertir el documento XML a un archivo
            String outputXMLPath = "output.xml";
            transformToXML(document, outputXMLPath);

            System.out.println("XML generado correctamente en: " + outputXMLPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Leer el archivo de texto y crear un documento XML
    public static Document createXMLDocument(String inputFilePath, String namesFilePath) throws Exception {
        // Crear el documento XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        // Crear el elemento raíz <list> y agregar los atributos necesarios
        Element root = document.createElement("list");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xsi:noNamespaceSchemaLocation", "../xsd/multisell.xsd");
        document.appendChild(root);

        // Cargar los nombres de los items desde el archivo namesFilePath
        Map<String, String[]> itemNames = loadItemNames(namesFilePath);

        // Leer el archivo de texto línea por línea
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Procesar cada línea del archivo
                String[] parts = line.split("\t"); // Separar por tabulaciones

                // Crear el elemento <item>
                Element item = document.createElement("item");
                root.appendChild(item);

                // Crear el elemento <ingredient> y agregarlo al item
                Element ingredient = document.createElement("ingredient");
                ingredient.setAttribute("count", "1");  // Usamos un valor fijo de 1 para el count
                ingredient.setAttribute("id", "57");  // Usamos el tercer valor (índice 2) como id
                item.appendChild(ingredient);

                // Crear el elemento <production> y agregarlo al item
                Element production = document.createElement("production");
                production.setAttribute("count", "1");  // Usamos un valor fijo de 1 para el count
                production.setAttribute("id", parts[2]);  // Usamos el tercer valor (índice 2) como id
                item.appendChild(production);

                // Obtener el nombre del item y el additionalname utilizando el id de la tercera columna
                String itemId = parts[2];
                String[] itemNameInfo = itemNames.get(itemId);

                if (itemNameInfo != null) {
                    String itemName = itemNameInfo[0]; // El nombre del item
                    String additionalName = itemNameInfo[1]; // El additional name

                    // Generar el comentario para el XML
                    String commentText = additionalName.isEmpty() ?
                        itemName : itemName + " - " + additionalName;

                    // Crear un comentario con el nombre y el additionalname
                    Comment comment = document.createComment(" " + commentText + " ");
                    
                    // Insertar el comentario después de production (o ingredient)
                    item.appendChild(comment);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Error al leer el archivo de texto.");
        }

        return document;
    }

    // Cargar los nombres de los items desde el archivo
    public static Map<String, String[]> loadItemNames(String namesFilePath) throws IOException {
        Map<String, String[]> itemNames = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(namesFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Procesar la línea para extraer el id, el nombre y el additionalname
                String[] parts = line.split("\t");
                String id = parts[1].split("=")[1];  // Obtener el id
                String name = parts[2].split("=")[1].replace("[", "").replace("]", "");  // Obtener el nombre
                String additionalName = parts[3].split("=")[1].replace("[", "").replace("]", "");  // Obtener el additionalname
                itemNames.put(id, new String[]{name, additionalName});  // Guardar el id, nombre y additionalname en el mapa
            }
        }
        return itemNames;
    }

    // Transformar el documento XML y guardarlo en un archivo
    public static void transformToXML(Document document, String outputXMLPath) throws Exception {
        // Usar Transformer para convertir el Document en XML
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Establecer las propiedades de salida (por ejemplo, para indentar)
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        // Crear una fuente DOM para el documento XML
        DOMSource source = new DOMSource(document);

        // Crear un resultado de salida (archivo donde se guardará el XML)
        StreamResult result = new StreamResult(new File(outputXMLPath));

        // Realizar la transformación y guardar el XML
        transformer.transform(source, result);
    }
}

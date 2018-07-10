# Simple XML Importer

This simple project is a generic importer of XML files into MongoDB. It allows to map which elements should be written into a certain collection. Furthermore, a SAX Parser is used which allows to process large files (in contrast to the DOM file where the whole XML document has to fit into memory).

I created this project while playing with product catalogs (like BMEcat, eCl@ass, ETIM, ...) that are used for product data exchange between companies and for supply chain automation.

A good example of a product catalog can be found at https://www.busch-jaeger.de/service-tools/downloads/stammdaten/. It follows the ETIM 6.0 product classification standard.

## Build

Maven project that manages all dependencies:

```
mvn package
```

## Run

The XML Loader takes a JSON file as configuration, an example is given below.

```
{
        "mongoURI":"mongodb://localhost:27017/test?retryWrites=true",
        "database": "product_catalog",
        "xml_file": "/Users/christian.kurze/Downloads/2018-06_Busch-Jaeger_2018_ETIM_6.0.zip",
        "mapping": {
          "HEADER": "catalog",
          "PRODUCT": "product"
        },
        "dropCollections": true
}
```

The parameters are as following:
- monogURI: MongoDB URI following the URI connection string syntax
- database: The MongoDB database where to write the data
- xml_file: The absolute path to the **zipped** XML file to be imported
- mapping: A mapping of XML tags to collection names - all occurences of this tag will be written into the mentioned collections
- dropCollections: Boolean to indicate if the collections should be dropped before inserting data

Start:

```
java -jar XMLImportBulk.jar -c config/sample.json
```

## Future Improvements
- More clever handling of the stack (not needed elements are written to the internal stack consuming memory)
 
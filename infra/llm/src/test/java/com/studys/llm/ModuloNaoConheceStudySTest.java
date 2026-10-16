package com.studys.llm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

class ModuloNaoConheceStudySTest {

    private static final Pattern IMPORT = Pattern.compile("import\\s+(?:static\\s+)?([^\\s;]+)\\s*;");

    private static List<String> importsDoArquivo(Path arquivo) throws Exception {
        List<String> importados = new ArrayList<>();
        for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
            Matcher achado = IMPORT.matcher(linha.stripLeading());
            if (achado.lookingAt()) {
                importados.add(achado.group(1));
            }
        }
        return importados;
    }

    @Test
    @DisplayName("nenhum arquivo de main importa outro módulo do StudyS")
    void nenhumArquivoDeMainImportaOutroModuloDoStudyS() throws Exception {
        Path raiz = Path.of("src/main/java");
        List<String> vazamentos = new ArrayList<>();
        try (Stream<Path> arquivos = Files.walk(raiz)) {
            List<Path> fontes = arquivos
                    .filter(arquivo -> arquivo.toString().endsWith(".java"))
                    .sorted()
                    .toList();
            for (Path arquivo : fontes) {
                for (String importado : importsDoArquivo(arquivo)) {
                    if (importado.startsWith("com.studys.") && !importado.startsWith("com.studys.llm.")) {
                        vazamentos.add(raiz.relativize(arquivo) + " importa " + importado);
                    }
                }
            }
        }

        assertThat(vazamentos)
                .as("arquivos de main que importam outro modulo do StudyS: %s", vazamentos)
                .isEmpty();
    }

    @Test
    @DisplayName("o pom não declara dependência de nenhum módulo do StudyS")
    void oPomNaoDeclaraDependenciaDeNenhumModuloDoStudyS() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
        Element raiz = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(pom)))
                .getDocumentElement();
        NodeList dependencias = raiz.getElementsByTagName("dependency");
        List<String> vazamentos = new ArrayList<>();
        for (int i = 0; i < dependencias.getLength(); i++) {
            Element dependencia = (Element) dependencias.item(i);
            String grupo = textoDe(dependencia, "groupId");
            String artefato = textoDe(dependencia, "artifactId");
            if ("com.studys".equals(grupo) && !"studys-llm".equals(artefato)) {
                vazamentos.add(grupo + ":" + artefato);
            }
        }

        assertThat(vazamentos)
                .as("dependencias do pom em outro modulo do StudyS: %s", vazamentos)
                .isEmpty();
    }

    private static String textoDe(Element elemento, String marca) {
        NodeList nos = elemento.getElementsByTagName(marca);
        if (nos.getLength() == 0) {
            return "";
        }
        return nos.item(0).getTextContent().strip();
    }
}

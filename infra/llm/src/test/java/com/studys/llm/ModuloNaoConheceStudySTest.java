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

/**
 * Garante a regra de reuso da spec §12.4: o módulo {@code infra/llm} não depende de nenhum outro
 * módulo do StudyS, nem pelos imports de {@code src/main} nem pelas dependências do seu pom ou do
 * pom raiz (herdadas). É um teste de build, não decorativo: morde de verdade quando alguém adiciona
 * uma dependência proibida (ver passo 3 do plano da Task 6).
 */
class ModuloNaoConheceStudySTest {

    private static final Pattern IMPORT = Pattern.compile("import\\s+(?:static\\s+)?([^\\s;]+)\\s*;");
    // Qualquer referência a com.studys.* que não seja do próprio pacote com.studys.llm.
    private static final Pattern REFERENCIA_PROIBIDA = Pattern.compile("com\\.studys\\.(?!llm\\.)");

    @Test
    @DisplayName("nenhum arquivo de main importa ou referencia outro módulo do StudyS")
    void nenhumArquivoDeMainImportaOutroModuloDoStudyS() throws Exception {
        Path raiz = Path.of("src/main/java");
        List<String> vazamentos = new ArrayList<>();
        try (Stream<Path> arquivos = Files.walk(raiz)) {
            List<Path> fontes = arquivos
                    .filter(arquivo -> arquivo.toString().endsWith(".java"))
                    .sorted()
                    .toList();
            for (Path arquivo : fontes) {
                vazamentos.addAll(importsProibidos(arquivo));
                vazamentos.addAll(referenciasProibidas(arquivo));
            }
        }

        assertThat(vazamentos)
                .as("arquivos de main que importam/referenciam outro modulo do StudyS: %s", vazamentos)
                .isEmpty();
    }

    @Test
    @DisplayName("o pom não declara dependência de nenhum módulo do StudyS")
    void oPomNaoDeclaraDependenciaDeNenhumModuloDoStudyS() throws Exception {
        List<String> vazamentos = new ArrayList<>();
        vazamentos.addAll(dependenciasProibidas(Path.of("pom.xml")));
        // O pom raiz é herdado por todos os módulos: uma dependência com.studys nele vazaria a regra
        // para o infra/llm sem que ninguém notice no pom do módulo.
        Path raiz = Path.of("../../pom.xml").toAbsolutePath().normalize();
        if (Files.exists(raiz)) {
            vazamentos.addAll(dependenciasProibidas(raiz));
        }

        assertThat(vazamentos)
                .as("dependencias com.studys em pom: %s", vazamentos)
                .isEmpty();
    }

    private static List<String> importsProibidos(Path arquivo) throws Exception {
        List<String> importados = new ArrayList<>();
        for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
            Matcher achado = IMPORT.matcher(linha.stripLeading());
            if (achado.lookingAt()) {
                String importado = achado.group(1);
                if (importado.startsWith("com.studys.") && !importado.startsWith("com.studys.llm.")) {
                    importados.add(raizRelativo(arquivo) + " importa " + importado);
                }
            }
        }
        return importados;
    }

    private static List<String> referenciasProibidas(Path arquivo) throws Exception {
        List<String> vazamentos = new ArrayList<>();
        int linhaNumero = 0;
        for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
            linhaNumero++;
            String trecho = linha.stripLeading();
            if (trecho.isEmpty()
                    || trecho.startsWith("//")
                    || trecho.startsWith("/*")
                    || trecho.startsWith("*")
                    || trecho.startsWith("package")) {
                continue;
            }
            if (REFERENCIA_PROIBIDA.matcher(linha).find()) {
                vazamentos.add(raizRelativo(arquivo) + ":" + linhaNumero
                        + " referencia outro modulo do StudyS: " + trecho);
            }
        }
        return vazamentos;
    }

    private static List<String> dependenciasProibidas(Path pom) throws Exception {
        String conteudo = Files.readString(pom, StandardCharsets.UTF_8);
        Element raiz = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(conteudo)))
                .getDocumentElement();
        NodeList dependencias = raiz.getElementsByTagName("dependency");
        List<String> vazamentos = new ArrayList<>();
        for (int i = 0; i < dependencias.getLength(); i++) {
            Element dependencia = (Element) dependencias.item(i);
            String grupo = textoDe(dependencia, "groupId");
            String artefato = textoDe(dependencia, "artifactId");
            if ("com.studys".equals(grupo) && !"studys-llm".equals(artefato)) {
                vazamentos.add(pom + ": " + grupo + ":" + artefato);
            }
        }
        return vazamentos;
    }

    private static String raizRelativo(Path arquivo) {
        try {
            return Path.of("src/main/java").relativize(arquivo).toString();
        } catch (Exception e) {
            return arquivo.toString();
        }
    }

    private static String textoDe(Element elemento, String marca) {
        NodeList nos = elemento.getElementsByTagName(marca);
        if (nos.getLength() == 0) {
            return "";
        }
        return nos.item(0).getTextContent().strip();
    }
}

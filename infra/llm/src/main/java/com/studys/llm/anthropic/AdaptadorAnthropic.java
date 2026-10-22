package com.studys.llm.anthropic;

import com.studys.llm.AdaptadorProvedor;
import com.studys.llm.ConfiguracaoProvedor;
import com.studys.llm.Mensagem;
import com.studys.llm.MetodoHttp;
import com.studys.llm.ProvedorRecusou;
import com.studys.llm.Requisicao;
import com.studys.llm.RequisicaoHttp;
import com.studys.llm.Resposta;
import com.studys.llm.RespostaHttp;
import com.studys.llm.TipoProvedor;
import com.studys.llm.Tokens;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class AdaptadorAnthropic implements AdaptadorProvedor {

    private static final String VERSAO_API = "2023-06-01";

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public TipoProvedor tipo() {
        return TipoProvedor.ANTHROPIC;
    }

    @Override
    public RequisicaoHttp montarRequisicao(
            Requisicao req, ConfiguracaoProvedor cfg, String credencial) {
        if (credencial == null || credencial.isBlank()) {
            throw new ProvedorRecusou(0, "o provedor " + cfg.tipo() + " exige credencial");
        }
        String base = cfg.urlBase();
        if (base == null || base.isBlank()) {
            throw new ProvedorRecusou(0, "o provedor " + cfg.tipo() + " não tem url base configurada");
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (req.maxTokens() <= 0) {
            throw new ProvedorRecusou(0, "maxTokens deve ser maior que zero");
        }
        String modelo = req.modelo();
        if (modelo == null || modelo.isBlank()) {
            modelo = cfg.modeloPadrao();
        }
        if (modelo == null || modelo.isBlank()) {
            throw new ProvedorRecusou(0, "o provedor " + cfg.tipo() + " não tem modelo configurado");
        }
        List<String> sistemas = new ArrayList<>();
        ArrayNode mensagens = JSON.createArrayNode();
        for (Mensagem mensagem : req.mensagens()) {
            switch (mensagem.papel()) {
                case SISTEMA -> sistemas.add(mensagem.texto());
                case USUARIO -> mensagens.add(mensagemJson("user", mensagem.texto()));
                case ASSISTENTE -> mensagens.add(mensagemJson("assistant", mensagem.texto()));
            }
        }
        ObjectNode corpo = JSON.createObjectNode();
        corpo.put("model", modelo);
        corpo.put("max_tokens", req.maxTokens());
        if (!sistemas.isEmpty()) {
            corpo.put("system", String.join("\n\n", sistemas));
        }
        corpo.set("messages", mensagens);
        if (req.temperatura() != null) {
            corpo.put("temperature", req.temperatura());
        }
        return new RequisicaoHttp(
                MetodoHttp.POST,
                base + "/v1/messages",
                Map.of(
                        "x-api-key", credencial,
                        "anthropic-version", VERSAO_API,
                        "content-type", "application/json"),
                JSON.writeValueAsString(corpo),
                req.timeout());
    }

    private static ObjectNode mensagemJson(String papel, String texto) {
        ObjectNode mensagem = JSON.createObjectNode();
        mensagem.put("role", papel);
        mensagem.put("content", texto);
        return mensagem;
    }

    /**
     * Lê a resposta da API de mensagens: os blocos {@code text} viram o texto, os blocos
     * {@code thinking} viram o raciocínio, {@code stop_reason} vira o motivo de parada, e
     * {@code usage} vira os tokens. Status 400 ou acima, ou corpo 2xx ilegível — que não é JSON,
     * ou vazio —, vira {@link ProvedorRecusou} com o corpo cru, para a tradução de erros ler.
     *
     * <p>Exercitada sobre {@code gravado/anthropic-resposta.json}, uma captura sintética
     * escrita à mão — ainda não há chave real para gravar uma resposta verdadeira — fiel
     * à forma documentada da API. Quando houver chamada real, trocar o arquivo por uma
     * captura verdadeira.
     */
    @Override
    public Resposta lerResposta(RespostaHttp http) {
        if (http.status() >= 400) {
            throw new ProvedorRecusou(http.status(), http.corpo());
        }
        JsonNode raiz;
        try {
            raiz = JSON.readTree(http.corpo());
        } catch (JacksonException e) {
            throw new ProvedorRecusou(http.status(), http.corpo());
        }
        // Qualquer corpo 2xx que não seja um objeto JSON — null, [], "texto", 123 — é inusável:
        // falhar alto em vez de fingir sucesso com texto vazio e tokens zerados.
        if (!raiz.isObject()) {
            throw new ProvedorRecusou(http.status(), http.corpo());
        }
        List<String> textos = new ArrayList<>();
        List<String> raciocinios = new ArrayList<>();
        JsonNode conteudo = raiz.get("content");
        if (conteudo != null && conteudo.isArray()) {
            for (JsonNode bloco : conteudo) {
                switch (bloco.path("type").asString("")) {
                    case "text" -> textos.add(bloco.path("text").asString(""));
                    case "thinking" -> raciocinios.add(bloco.path("thinking").asString(""));
                    default -> {
                    }
                }
            }
        }
        int entrada = 0;
        int saida = 0;
        JsonNode uso = raiz.get("usage");
        if (uso != null && uso.isObject()) {
            entrada = uso.path("input_tokens").asInt(0);
            saida = uso.path("output_tokens").asInt(0);
        }
        String motivoDeParada = raiz.path("stop_reason").asString("");
        return new Resposta(
                String.join("\n\n", textos),
                String.join("\n\n", raciocinios),
                motivoDeParada,
                new Tokens(entrada, saida));
    }
}

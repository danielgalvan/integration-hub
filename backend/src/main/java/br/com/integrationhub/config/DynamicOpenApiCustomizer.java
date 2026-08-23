package br.com.integrationhub.config;

import br.com.integrationhub.integration.model.Endpoint;
import br.com.integrationhub.integration.model.EndpointParameter;
import br.com.integrationhub.integration.model.Integration;
import br.com.integrationhub.integration.service.EndpointService;
import br.com.integrationhub.integration.service.IntegrationService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DynamicOpenApiCustomizer
        implements OpenApiCustomizer {

    private static final String DYNAMIC_PATH =
            "/api/**";

    private final IntegrationService integrationService;
    private final EndpointService endpointService;

    public DynamicOpenApiCustomizer(
            IntegrationService integrationService,
            EndpointService endpointService) {

        this.integrationService = integrationService;
        this.endpointService = endpointService;
    }

    @Override
    public void customise(OpenAPI openApi) {

        Paths paths = openApi.getPaths();

        if (paths == null) {
            paths = new Paths();
            openApi.setPaths(paths);
        }

        /*
         * /api/** é apenas a implementação interna
         * utilizada pelo DynamicEndpointController.
         *
         * O consumidor deve visualizar somente
         * os endpoints realmente configurados.
         */
        paths.remove(DYNAMIC_PATH);

        List<Integration> integrations =
                integrationService.findAll();

        for (Integration integration : integrations) {

            if (!isActive(integration.getActive())) {
                continue;
            }

            List<Endpoint> endpoints =
                    endpointService.findByIntegrationId(
                            integration.getId()
                    );

            for (Endpoint endpoint : endpoints) {

                if (!isActive(endpoint.getActive())) {
                    continue;
                }

                if (!"GET".equalsIgnoreCase(
                        endpoint.getMethod()
                )) {
                    continue;
                }

                addGetEndpoint(
                        paths,
                        integration,
                        endpoint
                );
            }
        }
    }

    private void addGetEndpoint(
            Paths paths,
            Integration integration,
            Endpoint endpoint) {

        String fullPath =
                buildFullPath(
                        integration.getBasePath(),
                        endpoint.getPath()
                );

        Operation operation =
                new Operation()
                        .operationId(
                                buildOperationId(
                                        integration,
                                        endpoint
                                )
                        )
                        .summary(endpoint.getName())
                        .description(endpoint.getDescription())
                        .addTagsItem(integration.getName());

        addParameters(
                operation,
                endpoint.getParameters()
        );

        operation.setResponses(
                createResponses()
        );

        PathItem pathItem =
                paths.get(fullPath);

        if (pathItem == null) {
            pathItem = new PathItem();
        }

        pathItem.setGet(operation);

        paths.addPathItem(
                fullPath,
                pathItem
        );
    }

    private void addParameters(
            Operation operation,
            List<EndpointParameter> parameters) {

        if (parameters == null
                || parameters.isEmpty()) {

            return;
        }

        for (EndpointParameter endpointParameter
                : parameters) {

            Parameter parameter =
                    new Parameter()
                            .name(
                                    endpointParameter.getName()
                            )
                            .in("query")
                            .required(
                                    endpointParameter.isRequired()
                            )
                            .schema(
                                    createSchema(
                                            endpointParameter.getType()
                                    )
                            );

            operation.addParametersItem(
                    parameter
            );
        }
    }

    private Schema<?> createSchema(String type) {

        if (type == null) {
            return new StringSchema();
        }

        return switch (type.toUpperCase()) {

            case "NUMBER" ->
                    new NumberSchema();

            case "DATE" ->
                    new DateSchema();

            case "TIMESTAMP" ->
                    new DateTimeSchema();

            case "VARCHAR2",
                 "VARCHAR",
                 "CHAR" ->
                    new StringSchema();

            default ->
                    new StringSchema();
        };
    }

    private ApiResponses createResponses() {

        ApiResponses responses =
                new ApiResponses();

        responses.addApiResponse(
                "200",
                new ApiResponse()
                        .description("Consulta executada com sucesso")
                        .content(
                                new Content()
                                        .addMediaType(
                                                "application/json",
                                                new MediaType()
                                                        .schema(
                                                                new ArraySchema()
                                                                        .items(
                                                                                new ObjectSchema()
                                                                        )
                                                        )
                                        )
                        )
        );

        responses.addApiResponse(
                "400",
                new ApiResponse()
                        .description(
                                "Parâmetro inválido ou obrigatório não informado"
                        )
        );

        responses.addApiResponse(
                "404",
                new ApiResponse()
                        .description(
                                "Integração ou endpoint não encontrado"
                        )
        );

        responses.addApiResponse(
                "500",
                new ApiResponse()
                        .description(
                                "Erro durante a execução da consulta"
                        )
        );

        return responses;
    }

    private String buildFullPath(
            String basePath,
            String endpointPath) {

        if (endpointPath == null
                || endpointPath.isBlank()
                || "/".equals(endpointPath)) {

            return basePath;
        }

        if (endpointPath.startsWith("/")) {
            return basePath + endpointPath;
        }

        return basePath + "/" + endpointPath;
    }

    private String buildOperationId(
            Integration integration,
            Endpoint endpoint) {

        return "dynamicGet_"
                + integration.getId()
                + "_"
                + endpoint.getId();
    }

    private boolean isActive(String active) {

        return "S".equalsIgnoreCase(active);
    }
}
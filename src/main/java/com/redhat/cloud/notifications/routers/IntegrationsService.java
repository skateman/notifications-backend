package com.redhat.cloud.notifications.routers;

import com.redhat.cloud.notifications.auth.RhIdPrincipal;
import com.redhat.cloud.notifications.db.EndpointResources;
import com.redhat.cloud.notifications.db.NotificationResources;
import com.redhat.cloud.notifications.models.Endpoint;
import com.redhat.cloud.notifications.models.NotificationHistory;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

@Path("/api/integrations/v1.0")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
// Email endpoints are not added at this point
// TODO Needs documentation annotations
public class IntegrationsService {

    @Inject
    EndpointResources resources;

    @Inject
    NotificationResources notifResources;

    @GET
    @Path("openapi.json")
    public StreamingOutput getOpenAPI() {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                InputStream is = getClass().getResourceAsStream("/openapi.integrations.json");
                is.transferTo(output);
            }
        };
    }

    @GET
    @RolesAllowed("read")
    @Parameters({
            @Parameter(
                    name = "pageSize",
                    in = ParameterIn.QUERY,
                    description = "Number of items per page, if not specified or 0 is used, returns all elements",
                    schema = @Schema(type = SchemaType.INTEGER)
            ),
            @Parameter(
                    name = "pageNumber",
                    in = ParameterIn.QUERY,
                    description = "Page number. Starts at first page (0), if not specified starts at first page.",
                    schema = @Schema(type = SchemaType.INTEGER)
            )
    })
    public Multi<Endpoint> getEndpoints(@Context SecurityContext sec, @Context UriInfo uriInfo) {
        RhIdPrincipal principal = (RhIdPrincipal) sec.getUserPrincipal();
        return resources.getEndpoints(principal.getAccount(), ParamUtils.parseQueryParams(uriInfo));
    }

    @POST
    @RolesAllowed("write")
    public Uni<Endpoint> createEndpoint(@Context SecurityContext sec, @NotNull @Valid Endpoint endpoint) {
        RhIdPrincipal principal = (RhIdPrincipal) sec.getUserPrincipal();
        endpoint.setTenant(principal.getAccount());
        return resources.createEndpoint(endpoint);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("read")
    public Uni<Endpoint> getEndpoint(@Context SecurityContext sec, @PathParam("id") UUID id) {
        RhIdPrincipal principal = (RhIdPrincipal) sec.getUserPrincipal();
        return resources.getEndpoint(principal.getAccount(), id)
                .onItem().ifNull().failWith(new NotFoundException());
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("write")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.STRING)))
    public Uni<Response> deleteEndpoint(@Context SecurityContext sec, @PathParam("id") UUID id) {
        RhIdPrincipal principal = (RhIdPrincipal) sec.getUserPrincipal();
        return resources.deleteEndpoint(principal.getAccount(), id)
                // onFailure() ?
                .onItem().transform(ignored -> Response.ok().build());
    }

    @PUT
    @Path("/{id}/enable")
    @RolesAllowed("write")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.STRING)))
    public Uni<Response> enableEndpoint(@Context SecurityContext sec, @PathParam("id") UUID id) {
        RhIdPrincipal principal = (RhIdPrincipal) sec.getUserPrincipal();
        return resources.enableEndpoint(principal.getAccount(), id)
                .onItem().transform(ignored -> Response.ok().build());
    }

    @DELETE
    @Path("/{id}/enable")
    @RolesAllowed("write")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.STRING)))
    public Uni<Response> disableEndpoint(@Context SecurityContext sec, @PathParam("id") UUID id) {
        RhIdPrincipal principal = (RhIdPrincipal) sec.getUserPrincipal();
        return resources.disableEndpoint(principal.getAccount(), id)
                .onItem().transform(ignored -> Response.ok().build());
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("write")
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.STRING)))
    public Uni<Response> updateEndpoint(@Context SecurityContext sec, @PathParam("id") UUID id, @NotNull @Valid Endpoint endpoint) {
        RhIdPrincipal principal = (RhIdPrincipal) sec.getUserPrincipal();
        endpoint.setTenant(principal.getAccount());
        endpoint.setId(id);
        return resources.updateEndpoint(endpoint)
                .onItem().transform(ignored -> Response.ok().build());
    }

    @GET
    @Path("/{id}/history")
    @RolesAllowed("read")
    public Multi<NotificationHistory> getEndpointHistory(@Context SecurityContext sec, @PathParam("id") UUID id) {
        // TODO We need globally limitations (Paging support and limits etc)
        RhIdPrincipal principal = (RhIdPrincipal) sec.getUserPrincipal();
        return notifResources.getNotificationHistory(principal.getAccount(), id);
    }

    @GET
    @Path("/{id}/history/{history_id}/details")
    @RolesAllowed("read")
    @Parameters({
            @Parameter(
                    name = "pageSize",
                    in = ParameterIn.QUERY,
                    description = "Number of items per page, if not specified or 0 is used, returns all elements",
                    schema = @Schema(type = SchemaType.INTEGER)
            ),
            @Parameter(
                    name = "pageNumber",
                    in = ParameterIn.QUERY,
                    description = "Page number. Starts at first page (0), if not specified starts at first page.",
                    schema = @Schema(type = SchemaType.INTEGER)
            )
    })
    @APIResponse(responseCode = "200", content = @Content(schema = @Schema(type = SchemaType.STRING)))
    public Uni<Response> getDetailedEndpointHistory(@Context SecurityContext sec, @PathParam("id") UUID id, @PathParam("history_id") Integer historyId, @Context UriInfo uriInfo) {
        RhIdPrincipal principal = (RhIdPrincipal) sec.getUserPrincipal();
        return notifResources.getNotificationDetails(principal.getAccount(), ParamUtils.parseQueryParams(uriInfo), id, historyId)
                // Maybe 404 should only be returned if history_id matches nothing? Otherwise 204
                .onItem().ifNull().failWith(new NotFoundException())
                .onItem().transform(json -> {
                    if (json.isEmpty()) {
                        return Response.noContent().build();
                    }
                    return Response.ok(json).build();
                });
    }
}
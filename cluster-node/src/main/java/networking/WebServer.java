package networking;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import model.proto.SearchModel.Request;
import model.proto.SearchModel.Response;
import model.proto.SearchServiceGrpc;


import java.io.IOException;

public class WebServer {
    private final int port;
    private final Server server;
    private final OnRequestCallback requestCallback;

    public WebServer(int port, OnRequestCallback requestCallback) {
        this.port = port;
        this.requestCallback = requestCallback;
        this.server = ServerBuilder.forPort(port)
                .addService(new SearchServiceImpl())
                .build();
    }

    public void startServer() throws IOException {
        server.start();
        System.out.println("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server since JVM is shutting down");
            try {
                WebServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.err.println("Server shut down");
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    private class SearchServiceImpl extends SearchServiceGrpc.SearchServiceImplBase {
        @Override
        public void search(Request request, StreamObserver<Response> responseObserver) {
            // Handle the request using the callback
            byte[] requestPayload = request.toByteArray();
            byte[] responseBytes = requestCallback.handleRequest(requestPayload);
            Response response;
            try {
                response = Response.parseFrom(responseBytes);
            } catch (IOException e) {
                responseObserver.onError(e);
                return;
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}

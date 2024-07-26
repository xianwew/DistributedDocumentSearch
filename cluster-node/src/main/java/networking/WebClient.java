package networking;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import model.proto.SearchModel.Request;
import model.proto.SearchModel.Response;
import model.proto.SearchServiceGrpc;

import java.util.concurrent.CompletableFuture;

public class WebClient {
    private final ManagedChannel channel;
    private final SearchServiceGrpc.SearchServiceStub asyncStub;

    public WebClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.asyncStub = SearchServiceGrpc.newStub(channel);
    }

    public CompletableFuture<Response> sendTask(byte[] payload) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        Request request;
        try {
            request = Request.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            future.completeExceptionally(e);
            return future;
        }

        asyncStub.search(request, new StreamObserver<Response>() {
            @Override
            public void onNext(Response response) {
                future.complete(response);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                // No-op
            }
        });

        return future;
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }
}

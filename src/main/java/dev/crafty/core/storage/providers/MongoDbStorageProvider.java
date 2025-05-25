package dev.crafty.core.storage.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import dev.crafty.core.storage.AbstractStorageProvider;
import dev.crafty.core.storage.serialization.StorageSerializer;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * An asynchronous MongoDB storage provider implementation using the Reactive Streams driver.
 * Handles serialization and deserialization of objects to and from MongoDB documents.
 *
 * @param <T> The type of objects to store.
 * @since 1.0.0
 */
public class MongoDbStorageProvider<T> extends AbstractStorageProvider<T, String> {

    private final MongoCollection<Document> collection;

    /**
     * Constructs a new AsyncMongoStorageProvider.
     *
     * @param valueType     The class type of the value to store.
     * @param tableName     The name of the MongoDB collection.
     * @param connectionUrl The MongoDB connection URL.
     */
    public MongoDbStorageProvider(
            Class<T> valueType,
            String tableName,
            String connectionUrl
    ) {
        super(valueType);

        var codecRegistry = CodecRegistries.fromRegistries(
                MongoClients.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(connectionUrl))
                        .codecRegistry(codecRegistry)
                        .build()
        );

        MongoDatabase database = mongoClient.getDatabase("master");
        this.collection = database.getCollection(tableName, Document.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> initialize() {
        return super.initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> save(String key, T value) {
        Document filter = new Document("_id", key);
        String json = null;
        try {
            json = StorageSerializer.toJson(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Document document = Document.parse(json);
        document.put("_id", key);

        CompletableFuture<Void> future = new CompletableFuture<>();

        collection.replaceOne(filter, document, new ReplaceOptions().upsert(true))
                .subscribe(toCompletableFutureSubscriber(future));

        return future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<T>> get(String key) {
        CompletableFuture<Optional<T>> future = new CompletableFuture<>();

        Document filter = new Document("_id", key);

        collection.find(filter).first()
                .subscribe(new Subscriber<>() {
                    private T result = null;
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(Document document) {
                        try {
                            result = StorageSerializer.fromJson(document.toJson(), valueType);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onComplete() {
                        future.complete(Optional.ofNullable(result));
                    }
                });

        return future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Collection<T>> getAll() {
        CompletableFuture<Collection<T>> future = new CompletableFuture<>();

        List<T> results = new ArrayList<>();

        collection.find()
                .subscribe(new Subscriber<>() {
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Document document) {
                        try {
                            T value = StorageSerializer.fromJson(document.toJson(), valueType);
                            results.add(value);
                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onComplete() {
                        future.complete(results);
                    }
                });

        return future;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> delete(String key) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Document filter = new Document("_id", key);

        collection.deleteOne(filter)
                .subscribe(toCompletableFutureSubscriber(future));

        return future;
    }

    /**
     * Utility method to create a Subscriber that completes the given CompletableFuture
     * when the operation completes or fails.
     *
     * @param future The CompletableFuture to complete.
     * @param <T>    The type emitted by the Publisher.
     * @return A Subscriber that bridges the Publisher to the CompletableFuture.
     */
    private static <T> Subscriber<T> toCompletableFutureSubscriber(CompletableFuture<Void> future) {
        return new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1); // request the operation to start
            }

            @Override
            public void onNext(T t) {
                // Ignore the emitted item
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                future.complete(null);
            }
        };
    }

}
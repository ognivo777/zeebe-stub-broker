package org.obiz.entity;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.obiz.BookService;
import org.obiz.Sample;

@GrpcService
public class BookServiceShouldNeverExists implements BookService {
    @Override
    public Uni<Sample.Book> getBook(Sample.GetBookRequest request) {
        return null;
    }

    @Override
    public Multi<Sample.Book> getBooksViaAuthor(Sample.GetBookViaAuthor request) {
        return null;
    }

    @Override
    public Uni<Sample.Book> getGreatestBook(Multi<Sample.GetBookRequest> request) {
        return null;
    }

    @Override
    public Multi<Sample.Book> getBooks(Multi<Sample.GetBookRequest> request) {
        return null;
    }
}

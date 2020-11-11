package com.iqvia;

import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.feed.dsl.Feed;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.integration.metadata.PropertiesPersistingMetadataStore;
import org.springframework.integration.transformer.AbstractPayloadTransformer;
import org.springframework.messaging.MessageHandler;

import java.io.File;

//import java.lang.reflect.ReflectAccess;

@SpringBootApplication
public class NewsReaderApplication {

    //private ReflectAccess tempFolder;

    public static void main(String[] args) {
        SpringApplication.run(NewsReaderApplication.class, args);
    }


    @Value("https://www.aljazeera.net/aljazeerarss/a7c186be-1baa-4bd4-9d80-a84db769f779/73d0e1b4-532f-45ef-b135-bfdff8b8cab9")
    private Resource feedResource;

//    @Bean
//    public MetadataStore metadataStore() {
//        PropertiesPersistingMetadataStore metadataStore = new PropertiesPersistingMetadataStore();
//        metadataStore.setBaseDirectory("/Desktop/temp");
//        return metadataStore;
//    }

//    @Bean
//    public IntegrationFlow feedFlow() {
//        return IntegrationFlows
//                .from(Feed.inboundAdapter(this.feedResource, "feedTest")
//                                .metadataStore(metadataStore()),
//                        e -> e.poller(p -> p.fixedDelay(100)))
//                .channel(c -> c.queue("entries"))
//                .get();
//    }

    @Bean
    public IntegrationFlow feedFlow() {
        return IntegrationFlows
                .from(Feed.inboundAdapter(this.feedResource, "news"), e -> e.poller(p -> p.fixedDelay(5000)))
                .transform(extractLinkFromFeed()).handle(targetDirectory())
                // .handle(System.out::println)
                .get();
    }

    @Bean
    public MessageHandler targetDirectory() {
        FileWritingMessageHandler handler = new FileWritingMessageHandler(
                new File("/temp"));
        handler.setAutoCreateDirectory(true);
         //handler.setCharset("UTF-8");
        handler.setExpectReply(false);
        return handler;
    }

    @Bean
    public AbstractPayloadTransformer<SyndEntry, String> extractLinkFromFeed() {
        return new AbstractPayloadTransformer<SyndEntry, String>() {
            @Override
            protected String transformPayload(SyndEntry payload) {
                return payload.getTitle() + " " + payload.getAuthor() + " " + payload.getLink();
            }
        };

    }

}

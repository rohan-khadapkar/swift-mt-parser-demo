package com.example.swiftmtparserdemo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import gr.datamation.mt.common.InvalidMessageFormatException;
import gr.datamation.mt.common.SwiftMessage;
import gr.datamation.mt.processor.SwiftMsgProcessor;

public class FinalpoDemo {
    public static void main(String[] args) throws IOException, InvalidMessageFormatException {
        // Docs at https://github.com/Payment-Components/demo-swift-mt
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources("classpath:/docs/**");

        for (Resource resource : resources) {
            System.out.println("Parsing file " + resource.getFilename());

            Path fileName = Path.of(resource.getURI());
            String swiftMtString = Files.readString(fileName);

            SwiftMsgProcessor parser = new SwiftMsgProcessor();
            SwiftMessage smObj = parser.ParseMsgStringToObject(swiftMtString);

            System.out.println("Sender " + smObj.getArgLTaddrBlk1());
            System.out.println("Receiver " + smObj.getArgLTaddrBlk2());
            System.out.println("MT: " + smObj.getArgMsgtype());

            System.out.println("---------------------------------------------------------------");
        }
    }
}

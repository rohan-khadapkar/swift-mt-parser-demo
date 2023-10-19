package com.example.swiftmtparserdemo;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.prowidesoftware.swift.model.mt.AbstractMT;
import com.prowidesoftware.swift.utils.Lib;

public class ProwideCodeDemo {
    public static void main(String[] args) throws IOException {
        // Docs at https://dev.prowidesoftware.com/SRU2022/open-source/core/mt-parser/
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources("classpath:/docs/**");

        for (Resource resource : resources) {
            System.out.println("Parsing file " + resource.getFilename());

            AbstractMT msg = AbstractMT.parse(Lib.readResource("docs/" + resource.getFilename(), null));
            System.out.println("Sender: " + msg.getSender());
            System.out.println("Receiver: " + msg.getReceiver());
            System.out.println("MT: " + msg.getMessageType());

            System.out.println("---------------------------------------------------------------");
        }
    }
}

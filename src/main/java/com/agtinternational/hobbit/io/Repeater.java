package com.agtinternational.hobbit.io;

import com.agtinternational.hobbit.core.Communication;

/**
 * @author Roman Katerinenko
 */
public class Repeater extends MinimalCommunication {
    private final Communication outputCommunication1;
    private final Communication outputCommunication2;

    private Repeater(Builder builder) {
        super(builder);
        this.outputCommunication1 = builder.outputCommunication1;
        this.outputCommunication2 = builder.outputCommunication2;
    }

    @Override
    public void close() throws Exception {
        outputCommunication1.close();
        outputCommunication2.close();
    }


    @Override
    public void send(byte[] bytes) throws Exception {
        outputCommunication1.send(bytes);
        outputCommunication2.send(bytes);
    }

    @Override
    public void send(String string) throws Exception {
        send(string.getBytes(getCharset()));
    }

    public final static class Builder extends MinimalCommunication.Builder {
        private Communication outputCommunication1;
        private Communication outputCommunication2;

        public Repeater.Builder outputCommunication1(Communication communication) {
            this.outputCommunication1 = communication;
            return this;
        }

        public Repeater.Builder outputCommunication2(Communication communication) {
            this.outputCommunication2 = communication;
            return this;
        }

        @Override
        public Repeater build() {
            return new Repeater(this);
        }
    }
}
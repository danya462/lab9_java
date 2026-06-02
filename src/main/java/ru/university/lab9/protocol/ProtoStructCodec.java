package ru.university.lab9.protocol;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import ru.university.lab9.model.ProtocolRequest;
import ru.university.lab9.model.ProtocolResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public final class ProtoStructCodec {
    private ProtoStructCodec() {
    }

    public static void writeRequest(DataOutputStream outputStream, ProtocolRequest request) throws IOException {
        Struct payload = Struct.newBuilder()
                .putFields("command", stringValue(request.command()))
                .putFields("argument", stringValue(request.argument()))
                .build();
        writeStruct(outputStream, payload);
    }

    public static ProtocolRequest readRequest(DataInputStream inputStream) throws IOException {
        Struct payload = readStruct(inputStream);
        if (payload == null) {
            return null;
        }
        return new ProtocolRequest(readString(payload, "command"), readString(payload, "argument"));
    }

    public static void writeResponse(DataOutputStream outputStream, ProtocolResponse response) throws IOException {
        Struct payload = Struct.newBuilder()
                .putFields("success", Value.newBuilder().setBoolValue(response.success()).build())
                .putFields("message", stringValue(response.message()))
                .build();
        writeStruct(outputStream, payload);
    }

    public static ProtocolResponse readResponse(DataInputStream inputStream) throws IOException {
        Struct payload = readStruct(inputStream);
        if (payload == null) {
            return null;
        }
        boolean success = payload.containsFields("success") && payload.getFieldsOrThrow("success").getBoolValue();
        return new ProtocolResponse(success, readString(payload, "message"));
    }

    private static void writeStruct(DataOutputStream outputStream, Struct payload) throws IOException {
        byte[] bytes = payload.toByteArray();
        outputStream.writeInt(bytes.length);
        outputStream.write(bytes);
        outputStream.flush();
    }

    private static Struct readStruct(DataInputStream inputStream) throws IOException {
        try {
            int length = inputStream.readInt();
            byte[] bytes = inputStream.readNBytes(length);
            if (bytes.length < length) {
                throw new EOFException("Неожиданный конец ProtoBuf-кадра.");
            }
            return Struct.parseFrom(bytes);
        } catch (EOFException exception) {
            return null;
        } catch (InvalidProtocolBufferException exception) {
            throw new IOException("Некорректная ProtoBuf-нагрузка", exception);
        }
    }

    private static Value stringValue(String value) {
        return Value.newBuilder().setStringValue(value == null ? "" : value).build();
    }

    private static String readString(Struct payload, String key) {
        return payload.containsFields(key) ? payload.getFieldsOrThrow(key).getStringValue() : "";
    }
}

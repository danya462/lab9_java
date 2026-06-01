package ru.university.lab9.server;

import ru.university.lab9.model.ProtocolResponse;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnonymousVotingService {
    private final Set<String> participants = ConcurrentHashMap.newKeySet();
    private final Set<String> candidates = ConcurrentHashMap.newKeySet();
    private final Set<String> voters = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> voteCounts = new ConcurrentHashMap<>();

    public synchronized ProtocolResponse register(String nickname) {
        String normalized = normalize(nickname);
        if (normalized == null) {
            return new ProtocolResponse(false, "Nickname is required.");
        }
        boolean added = participants.add(normalized);
        return new ProtocolResponse(true, added
                ? normalized + " registered."
                : normalized + " already registered.");
    }

    public synchronized ProtocolResponse nominate(String actor, String candidate) {
        String normalizedActor = normalize(actor);
        String normalizedCandidate = normalize(candidate);
        if (normalizedActor == null || normalizedCandidate == null) {
            return new ProtocolResponse(false, "Actor and candidate are required.");
        }
        if (!participants.contains(normalizedActor)) {
            return new ProtocolResponse(false, "Register first.");
        }
        if (!participants.contains(normalizedCandidate)) {
            return new ProtocolResponse(false, "Candidate is not registered.");
        }

        candidates.add(normalizedCandidate);
        voteCounts.putIfAbsent(normalizedCandidate, 0);
        return new ProtocolResponse(true, normalizedCandidate + " added to the ballot.");
    }

    public synchronized ProtocolResponse vote(String actor, String candidate) {
        String normalizedActor = normalize(actor);
        String normalizedCandidate = normalize(candidate);
        if (normalizedActor == null || normalizedCandidate == null) {
            return new ProtocolResponse(false, "Actor and candidate are required.");
        }
        if (!participants.contains(normalizedActor)) {
            return new ProtocolResponse(false, "Register first.");
        }
        if (!candidates.contains(normalizedCandidate)) {
            return new ProtocolResponse(false, "Candidate is not nominated.");
        }
        if (!voters.add(normalizedActor)) {
            return new ProtocolResponse(false, "This user has already voted.");
        }

        voteCounts.merge(normalizedCandidate, 1, Integer::sum);
        return new ProtocolResponse(true, "Vote accepted anonymously.");
    }

    public synchronized ProtocolResponse results() {
        if (voteCounts.isEmpty()) {
            return new ProtocolResponse(true, "No votes yet.");
        }

        Map<String, Integer> sorted = voteCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);

        StringBuilder builder = new StringBuilder();
        sorted.forEach((candidate, votes) -> {
            if (!builder.isEmpty()) {
                builder.append("; ");
            }
            builder.append(candidate).append(" = ").append(votes);
        });
        return new ProtocolResponse(true, builder.toString());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

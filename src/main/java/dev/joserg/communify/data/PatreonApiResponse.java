package dev.joserg.communify.data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

public class PatreonApiResponse {

    @Data
    public static class Identity {
        IdentityData data;
        List<ObjectNode> included;
    }

    @Data
    public static class IdentityData {
        Long id;
        IdentityDataAttributes attributes;
        Relationship relationships;
    }

    @Data
    public static class IdentityDataAttributes {
        String email;
        String full_name;
    }

    @Data
    public static class Relationship {
        Campaign campaign;
        Memberships memberships;
    }

    @Data
    public static class Memberships {
        List<MembershipData> data;
    }

    @Data
    public static class Campaign {
        CampaignData data;
    }

    @Data
    public static class CampaignData {
        String id;
    }

    @Data
    public static class MembershipData {
        String id;
    }
}

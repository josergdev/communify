package dev.joserg.communify.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionDetails {
    Boolean patreonIsConnected;
}

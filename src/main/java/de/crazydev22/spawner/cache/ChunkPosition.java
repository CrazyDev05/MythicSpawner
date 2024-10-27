package de.crazydev22.spawner.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChunkPosition {
    private UUID world = null;
    private int x = 0;
    private int z = 0;
}

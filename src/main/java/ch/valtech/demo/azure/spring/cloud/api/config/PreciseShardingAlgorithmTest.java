package ch.valtech.demo.azure.spring.cloud.api.config;

import java.util.Collection;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.springframework.stereotype.Component;

@Component
public class PreciseShardingAlgorithmTest implements PreciseShardingAlgorithm<String> {

    public PreciseShardingAlgorithmTest() {
    }

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        return shardingValue.getValue().equals("de") ? "ds1" : "ds0";
    }

}

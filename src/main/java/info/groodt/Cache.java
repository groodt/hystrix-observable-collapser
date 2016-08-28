package info.groodt;

import java.util.Map;
import java.util.Set;

public interface Cache {

    Map<String, String> getValues(final Set<String> keys);
}

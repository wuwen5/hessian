package io.github.wuwen5.hessian.io.beans.black;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author wuwen
 */
@Getter
@Setter
@Accessors(chain = true)
public class DenyObj implements Serializable {
    private String name;
}

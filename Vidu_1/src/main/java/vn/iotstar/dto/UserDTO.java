package vn.iotstar.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private Long id;
    private String email;
    private String fullName;
    private boolean enabled;
    private Long roleId;
    private String roleName;
}

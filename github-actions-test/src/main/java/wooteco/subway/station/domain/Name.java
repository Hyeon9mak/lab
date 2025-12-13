package wooteco.subway.station.domain;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import wooteco.subway.exception.InvalidNameException;

public class Name {

    private static final String PATTERN = "^[가-힣0-9]*$";

    @NotNull(message = "이름은 Null 일 수 없습니다.")
    private final String value;

    public Name(String value) {
        this.value = value;
//        validateForm(this.value);
    }

    private void validateForm(String value) {
        if (value.matches(PATTERN)) {
            return;
        }

        throw new InvalidNameException();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Name name = (Name) o;
        return Objects.equals(value, name.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

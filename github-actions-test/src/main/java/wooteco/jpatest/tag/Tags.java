package wooteco.jpatest.tag;

import java.util.List;

public class Tags {

    private final List<Tag> tags;

    public Tags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Tag> toList() {
        return tags;
    }
}

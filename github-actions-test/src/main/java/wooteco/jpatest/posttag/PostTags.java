package wooteco.jpatest.posttag;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Embeddable
public class PostTags {

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    private final List<PostTag> values = new ArrayList<>();

    public PostTags() {
    }

    public void add(List<PostTag> postTags) {
        values.addAll(postTags);
    }

    public List<PostTag> toList() {
        return values;
    }

    public void clearPostTags() {
        values.clear();
    }
}

package wooteco.jpatest.post;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import wooteco.jpatest.posttag.PostTag;
import wooteco.jpatest.posttag.PostTags;
import wooteco.jpatest.tag.Tag;
import wooteco.jpatest.tag.Tags;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private final PostTags postTags = new PostTags();

    protected Post() {
    }

    public Post(List<Tag> tags) {
        this(null, tags);
    }

    public Post(Long id, List<Tag> tags) {
        this.id = id;
        addTags(new Tags(tags));
    }

    public void addTags(Tags tags) {
        postTags.add(convertToPostTags(tags));
    }

    private List<PostTag> convertToPostTags(Tags tags) {
        return tags.toList()
            .stream()
            .map(tag -> new PostTag(this, tag))
            .collect(Collectors.toList());
    }

    public void unLinkTags() {
        postTags.clearPostTags();
    }

    public Long getId() {

        return id;
    }

    public List<PostTag> getPostTags() {
        return postTags.toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return Objects.equals(id, post.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

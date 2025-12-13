package wooteco.jpatest.post;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import javax.persistence.CascadeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import wooteco.jpatest.posttag.PostTag;
import wooteco.jpatest.posttag.PostTagRepository;
import wooteco.jpatest.tag.Tag;
import wooteco.jpatest.tag.TagRepository;

@DataJpaTest
public class jpaTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void jpaTestTest() {
        // given
        Tag tag1 = tagRepository.save(new Tag("태그1"));
        Tag tag2 = tagRepository.save(new Tag("태그2"));
        Tag tag3 = tagRepository.save(new Tag("태그3"));

        Post post = postRepository.save(new Post(Arrays.asList(tag1, tag2, tag3)));

        // when
        assertThat(postTagRepository.findAll()).isEqualTo(post.getPostTags());

//        postRepository.delete(post); 부모객체의 삭제는 CasCade.Remove와 orphanremoval = true 모두 잘 작동
        post.unLinkTags();

        // then
        assertThat(postTagRepository.findAll()).isEmpty();

    }
}

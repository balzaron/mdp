package com.miotech.mdp;

import com.miotech.mdp.common.model.dao.TagsEntity;
import com.miotech.mdp.common.persistent.TagsRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SampleServiceTest extends ServiceTest {
    @Autowired
    private TagsRepository repo;

    @Test
    @Transactional // FIXME: 因为hibernate的懒加载机制，必须在每个测试前都加上 @Transactional
    public void test1() {
        // prepare
        TagsEntity entity = new TagsEntity();
        entity.setName("foo");
        entity.setColor("red");
        repo.saveAndFlush(entity);

        // verify
        TagsEntity result = repo.getOne(entity.getId());
        assertThat(result.getName(), is(entity.getName()));
        assertThat(result.getColor(), is(entity.getColor()));
    }
}

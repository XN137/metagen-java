package io.virtdata.core;

import io.virtdata.templates.StringCompositor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class StringCompositorTest {

    private BindingsTemplate template;
    private Bindings bindings;

    @BeforeClass
    public void setupTemplate() {
        BindingsTemplate bindingsTemplate = new BindingsTemplate(AllDataMapperLibraries.get());
        bindingsTemplate.addFieldBinding("ident","Identity()");
        bindingsTemplate.addFieldBinding("mod5", "Mod(5)");
        bindingsTemplate.addFieldBinding("mod-5", "Mod(5)");
        bindingsTemplate.addFieldBinding("5_mod_5", "Mod(5)");
        bindingsTemplate.addFieldBinding(".mod5", "Mod(5)");
        this.template = bindingsTemplate;
        this.bindings = bindingsTemplate.resolveBindings();
    }

    @Test
    public void testBindValues() {
        StringCompositor c = new StringCompositor("A{ident}C");
        String s = c.bindValues(c, bindings, 0L);
        assertThat(s).isEqualTo("A0C");
    }

    @Test
    public void testBindValuesSpecialChars() {
        StringCompositor c = new StringCompositor("A{mod-5}C");
        String s = c.bindValues(c, bindings, 6L);
        assertThat(s).isEqualTo("A1C");

        c = new StringCompositor("A{5_mod_5}C");
        s = c.bindValues(c, bindings, 7L);
        assertThat(s).isEqualTo("A2C");

        c = new StringCompositor("A{.mod5}C");
        s = c.bindValues(c, bindings, 8L);
        assertThat(s).isEqualTo("A3C");
    }
}
package net.study.mapping;

public interface DtoConverter <Dto,Entity> {
    Dto convertToDto(Entity entity);
}

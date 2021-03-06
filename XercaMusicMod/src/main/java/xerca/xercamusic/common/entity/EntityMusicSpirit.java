package xerca.xercamusic.common.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import xerca.xercamusic.client.NoteSound;
import xerca.xercamusic.common.XercaMusic;
import xerca.xercamusic.common.block.BlockInstrument;
import xerca.xercamusic.common.block.Blocks;
import xerca.xercamusic.common.item.ItemInstrument;
import xerca.xercamusic.common.item.Items;

public class EntityMusicSpirit extends Entity implements IEntityAdditionalSpawnData {
    private PlayerEntity body;
    private ItemStack note;
    private ItemInstrument instrument;
    private byte[] music;
    private int mLength;
    private int mTime;
    private byte mPause;
    private NoteSound lastPlayed = null;
    private boolean isPlaying = true;
    private BlockInstrument blockInstrument = null;
    private BlockPos blockInsPos = null;

    public EntityMusicSpirit(World worldIn) {
        super(Entities.MUSIC_SPIRIT, worldIn);
    }

    public EntityMusicSpirit(World worldIn, PlayerEntity body, ItemInstrument instrument) {
        super(Entities.MUSIC_SPIRIT, worldIn);
        this.body = body;
        this.instrument = instrument;
        setNoteFromBody();
        this.mTime = 0;
        this.setPosition(body.getPosX(), body.getPosY(), body.getPosZ());
        if (note.hasTag() && note.getTag().contains("music")) {
            CompoundNBT comp = note.getTag();
            music = comp.getByteArray("music");
            mLength = comp.getInt("length");
            mPause = comp.getByte("pause");
        }
    }

    public EntityMusicSpirit(World worldIn, PlayerEntity body, BlockPos blockInsPos, ItemInstrument instrument) {
        this(worldIn, body, instrument);
        setBlockPosAndInstrument(blockInsPos);
    }

    public EntityMusicSpirit(EntityType<EntityMusicSpirit> type, World world) {
        super(type, world);
    }

    public EntityMusicSpirit(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        this(world);
    }

    private void setBlockPosAndInstrument(BlockPos pos){
        this.blockInsPos = pos;
        Block block = world.getBlockState(blockInsPos).getBlock();

        if(block instanceof BlockInstrument) {
            blockInstrument = (BlockInstrument)block;
            setPosition((double)blockInsPos.getX()+0.5, (double)blockInsPos.getY()-0.5, (double)blockInsPos.getZ()+0.5);
        }
        else{
            XercaMusic.LOGGER.warn("Got invalid block as instrument");
            blockInstrument = null;
            blockInsPos = null;
        }
    }

    private boolean isBodyHandLegit(){
        ItemStack mainStack = body.getHeldItemMainhand();
        ItemStack offStack = body.getHeldItemOffhand();
        if(blockInstrument != null && blockInsPos != null){
            return mainStack.getItem() == Items.MUSIC_SHEET || offStack.getItem() == Items.MUSIC_SHEET;
        }
        else{
            return offStack.getItem() == Items.MUSIC_SHEET && mainStack.getItem() == instrument;
        }
    }

    private void setNoteFromBody(){
        ItemStack mainStack = body.getHeldItemMainhand();
        ItemStack offStack = body.getHeldItemOffhand();
        if(mainStack.getItem() == Items.MUSIC_SHEET){
            this.note = mainStack;
        }
        else if(offStack.getItem() == Items.MUSIC_SHEET){
            this.note = offStack;
        }
        else{
            XercaMusic.LOGGER.warn("No music sheet found on body");
        }
    }

    @Override
    protected void readAdditional(CompoundNBT tagCompound) {
        this.music = tagCompound.getByteArray("music");
        this.mLength = tagCompound.getInt("length");
        this.mPause = tagCompound.getByte("pause");
        this.isPlaying = tagCompound.getBoolean("playing");
        if(tagCompound.contains("bX") && tagCompound.contains("bY") && tagCompound.contains("bZ")){
            setBlockPosAndInstrument(new BlockPos(tagCompound.getInt("bX"), tagCompound.getInt("bY"), tagCompound.getInt("bZ")));
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT tagCompound) {
        tagCompound.putByteArray("music", this.music);
        tagCompound.putInt("length", mLength);
        tagCompound.putByte("pause", mPause);
        tagCompound.putBoolean("playing", isPlaying);
        if(blockInstrument != null && blockInsPos != null){
            tagCompound.putInt("bX", blockInsPos.getX());
            tagCompound.putInt("bY", blockInsPos.getY());
            tagCompound.putInt("bZ", blockInsPos.getZ());
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(body != null ? body.getEntityId() : -1);
        if(blockInstrument != null && blockInsPos != null){
            buffer.writeInt(blockInsPos.getX());
            buffer.writeInt(blockInsPos.getY());
            buffer.writeInt(blockInsPos.getZ());
        }
        else{
            buffer.writeInt(-1);
            buffer.writeInt(-1);
            buffer.writeInt(-1);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        int id = buffer.readInt();
        Entity ent = world.getEntityByID(id);
        if (ent instanceof PlayerEntity) {
            body = (PlayerEntity) ent;
        }

        int bx = buffer.readInt();
        int by = buffer.readInt();
        int bz = buffer.readInt();
        if(by >= 0){
            setBlockPosAndInstrument(new BlockPos(bx, by ,bz));
        }
        this.mTime = 0;

        if(blockInsPos != null){
            this.instrument = blockInstrument.getItemInstrument();
            this.setNoteFromBody();
        }
        else{
            this.instrument = (ItemInstrument) body.getHeldItemMainhand().getItem();
            this.note = body.getHeldItemOffhand();
            this.setPosition(body.getPosX(), body.getPosY(), body.getPosZ());
        }
        if (note.hasTag() && note.getTag().contains("music")) {
            CompoundNBT comp = note.getTag();
            music = comp.getByteArray("music");
            mLength = comp.getInt("length");
            mPause = comp.getByte("pause");
        }
    }

    @Override
    protected void registerData() {

    }

    @Override
    public void tick() {
        if (!this.world.isRemote) {
            if (body == null || !isPlaying) {
                this.remove();
                return;
            }
            if (!isBodyHandLegit()) {
                isPlaying = false;
                this.remove();
                return;
            }

            if(blockInsPos != null && blockInstrument != null){
                if(world.getBlockState(blockInsPos).getBlock() != blockInstrument){
                    this.remove();
                    return;
                }
                if(this.getPositionVec().distanceTo(this.body.getPositionVec()) > 4){
                    this.remove();
                    return;
                }
            }
        }
        super.tick();
        if(blockInsPos == null || blockInstrument == null){
            this.setPosition(body.getPosX(), body.getPosY(), body.getPosZ());
        }
        if (this.world.isRemote) {
            if(mPause == 0){
                System.err.println("EntityMusicSpirit mPause is 0! THIS SHOULD NOT HAPPEN!");
                return;
            }
            if (this.ticksExisted % mPause == 0) {
                if (mTime == mLength) {
                    XercaMusic.proxy.endMusic(getEntityId(), body.getEntityId());
                    this.remove();
                    return;
                }
                if (music[mTime] != 0 && music[mTime] <= 48) {
                    if(instrument.shouldCutOff && lastPlayed != null){
                        lastPlayed.stopSound();
                    }
                    lastPlayed = XercaMusic.proxy.playNote(instrument.getSound(music[mTime] - 1), getPosX(), getPosY() + 0.5d, getPosZ());
                    this.world.addParticle(ParticleTypes.NOTE, getPosX(), getPosY() + 2.2D, getPosZ(), (music[mTime] -1) / 24.0D, 0.0D, 0.0D);

                }
                mTime++;
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public PlayerEntity getBody() {
        return body;
    }

    public void setBody(PlayerEntity body) {
        this.body = body;
    }

}
